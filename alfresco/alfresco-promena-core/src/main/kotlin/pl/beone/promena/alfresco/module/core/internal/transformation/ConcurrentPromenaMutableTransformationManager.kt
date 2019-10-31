package pl.beone.promena.alfresco.module.core.internal.transformation

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ConcurrentPromenaMutableTransformationManager(
    bufferSize: Int,
    private val waitMax: Duration
) : PromenaMutableTransformationManager {

    private data class TransformationDescription(
        val lock: Lock,
        var result: TransformationExecutionResult? = null,
        var throwable: Throwable? = null
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val transformationDescriptionMap = MaxSizeHashMap<String, TransformationDescription>(bufferSize)
    private var globalId: Long = 1
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun getResult(transformationExecution: TransformationExecution, waitMax: Duration?): TransformationExecutionResult {
        val id = transformationExecution.id
        val determinedWaitMax = determineWaitMax(waitMax)

        val transformationDescriptor = transformationDescriptionMap[id] ?: throw IllegalStateException("There is no transformation <$id> in progress")
        return if (transformationDescriptor.lock.tryLock(determinedWaitMax.toMillis(), MILLISECONDS)) {
            transformationDescriptor.result ?: throw transformationDescriptor.throwable
                ?: IllegalStateException("There is no result or throwable for transformation <$id>")
        } else {
            throw TimeoutException("Waiting time for transformation <$id> has expired")
        }
    }

    private fun determineWaitMax(waitMax: Duration?): Duration =
        waitMax ?: this.waitMax

    override fun startTransformation(): TransformationExecution =
        runBlocking(dispatcher) {
            val id = globalId++.toString()
            val transformationExecution = transformationExecution(id)

            val transformationDescription = TransformationDescription(ReentrantLock())
            transformationDescriptionMap[id] = transformationDescription
            try {
                transformationDescription.lock.lock()
                logger.debug { "Started transformation <$id>" }
                transformationExecution
            } catch (e: Exception) {
                transformationDescriptionMap.remove(id)
                throw e
            }
        }

    override fun completeTransformation(transformationExecution: TransformationExecution, result: TransformationExecutionResult) {
        runBlocking(dispatcher) {
            val id = transformationExecution.id
            if (executeAndUnlock(id) { it.result = result }) {
                logger.debug { "Completed transformation <$id>: ${result.nodeRefs}" }
            } else {
                logger.warn { "Couldn't complete transformation. There is no transformation <$id> in progress" }
            }
        }
    }

    override fun completeErrorTransformation(transformationExecution: TransformationExecution, throwable: Throwable) {
        runBlocking(dispatcher) {
            val id = transformationExecution.id
            if (executeAndUnlock(id) { it.throwable = throwable }) {
                logger.debug(throwable) { "Completed <$id> transformation with the error" }
            } else {
                logger.warn(throwable) { "Couldn't complete transformation with an error. There is no <$id> transformation in progress" }
            }
        }
    }

    private fun executeAndUnlock(id: String, toExecute: (TransformationDescription) -> Unit): Boolean {
        val transformationDescription = transformationDescriptionMap[id]

        return if (transformationDescription != null) {
            toExecute(transformationDescription)
            transformationDescription.lock.unlock()
            true
        } else {
            false
        }
    }
}