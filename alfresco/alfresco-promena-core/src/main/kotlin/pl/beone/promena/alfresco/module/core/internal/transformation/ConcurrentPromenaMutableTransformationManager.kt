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

    private data class Transformation(
        val lock: Lock,
        var result: TransformationExecutionResult? = null,
        var throwable: Throwable? = null
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val transformationMap = MaxSizeHashMap<String, Transformation>(bufferSize)
    private var globalId: Long = 1
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun getResult(transformationExecution: TransformationExecution, waitMax: Duration?): TransformationExecutionResult {
        val id = transformationExecution.id
        val determinedWaitMax = determineWaitMax(waitMax)

        val transformation = transformationMap[id] ?: throw IllegalStateException("There is no <$id> transformation in progress")
        return if (transformation.lock.tryLock(determinedWaitMax.toMillis(), MILLISECONDS)) {
            transformation.result ?: throw transformation.throwable
                ?: IllegalStateException("There is no result or throwable for <$id> transformation")
        } else {
            throw TimeoutException("Waiting time for <$id> transformation has expired")
        }
    }

    private fun determineWaitMax(waitMax: Duration?): Duration =
        waitMax ?: this.waitMax

    override fun startTransformation(): TransformationExecution =
        runBlocking(dispatcher) {
            val id = globalId++.toString()
            val transformationExecution = transformationExecution(id)

            val transformation =
                Transformation(
                    ReentrantLock()
                )
            transformationMap[id] = transformation
            try {
                transformation.lock.lock()
                logger.debug { "Started <$id> transformation" }
                transformationExecution
            } catch (e: Exception) {
                transformationMap.remove(id)
                throw e
            }
        }

    override fun completeTransformation(transformationExecution: TransformationExecution, result: TransformationExecutionResult) {
        runBlocking(dispatcher) {
            val id = transformationExecution.id
            if (executeAndUnlock(id) { it.result = result }) {
                logger.debug { "Completed <$id> transformation: ${result.nodeRefs}" }
            } else {
                logger.warn { "Couldn't complete transformation. There is no <$id> transformation in progress" }
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

    private fun executeAndUnlock(id: String, toExecute: (Transformation) -> Unit): Boolean {
        val transformation = transformationMap[id]

        return if (transformation != null) {
            toExecute(transformation)
            transformation.lock.unlock()
            true
        } else {
            false
        }
    }
}