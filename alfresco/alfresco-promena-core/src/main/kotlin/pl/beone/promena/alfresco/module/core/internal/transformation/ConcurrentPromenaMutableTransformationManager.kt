package pl.beone.promena.alfresco.module.core.internal.transformation

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ConcurrentPromenaMutableTransformationManager(
    private val bufferSize: Int,
    private val waitMax: Duration
) : PromenaMutableTransformationManager {

    data class Transformation(
        val lock: Lock,
        var result: TransformationExecutionResult? = null,
        var throwable: Throwable? = null
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val transformationMap =
        MaxSizeHashMap<String, Transformation>(
            bufferSize
        )
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun getResult(transformationExecution: TransformationExecution, waitMax: Duration?): TransformationExecutionResult {
        val id = transformationExecution.id
        val determinedWaitMax = determineWaitMax(waitMax)

        val (lock, result, throwable) = transformationMap[id] ?: throw IllegalStateException("There is no <$id> transaction in progress")
        return if (lock.tryLock(determinedWaitMax.toMillis(), MILLISECONDS)) {
            result ?: throw throwable!!
        } else {
            throw TimeoutException("Waiting time for <$id> transaction has expired")
        }
    }

    private fun determineWaitMax(waitMax: Duration?): Duration =
        waitMax ?: this.waitMax

    override fun startTransformation(): TransformationExecution =
        runBlocking(dispatcher) {
            val id = generateId()
            val transformationExecution = TransformationExecution(id)

            val transformation =
                Transformation(ReentrantLock())
            transformationMap[id] = transformation
            try {
                transformation.lock.lock()
                transformationExecution
            } catch (e: Exception) {
                transformationMap.remove(id)
                throw e
            }
        }

    override fun completeTransformation(transformationExecution: TransformationExecution, result: TransformationExecutionResult) {
        runBlocking(dispatcher) {
            val id = transformationExecution.id
            unlockAndExecute(id) { it.result = result }
        }
    }

    override fun completeErrorTransformation(transformationExecution: TransformationExecution, throwable: Throwable) {
        runBlocking(dispatcher) {
            val id = transformationExecution.id
            unlockAndExecute(id) { it.throwable = throwable }
        }
    }

    private fun unlockAndExecute(id: String, toExecute: (Transformation) -> Unit) {
        val transformation = transformationMap[id]

        if (transformation != null) {
            transformation.lock.unlock()
            toExecute(transformation)
        } else {
            logger.warn { "There is no <$id> transaction in progress" }
        }
    }

    private fun generateId(): String =
        UUID.randomUUID().toString()
}