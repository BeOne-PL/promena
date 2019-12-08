package pl.beone.promena.alfresco.module.core.external.transformation.manager

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.alfresco.service.ServiceRegistry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.internal.transformation.MaxSizeHashMap
import pl.beone.promena.core.contract.serialization.SerializationService
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class MemoryWithAlfrescoPersistencePromenaMutableTransformationManager(
    private val persistInAlfresco: Boolean,
    bufferSize: Int,
    private val waitMax: Duration,
    serializationService: SerializationService,
    serviceRegistry: ServiceRegistry
) : PromenaMutableTransformationManager {

    internal data class TransformationDescription(
        val lock: Lock,
        var result: TransformationExecutionResult? = null,
        var throwable: Throwable? = null
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val alfrescoPersistence = AlfrescoPromenaManagerPersistence(serializationService, serviceRegistry)

    private val transformationDescriptionMap = MaxSizeHashMap<String, TransformationDescription>(bufferSize)
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun getResult(transformationExecution: TransformationExecution, waitMax: Duration?): TransformationExecutionResult {
        val executionId = transformationExecution.id
        val determinedWaitMax = determineWaitMax(waitMax)

        val transformationDescriptor = transformationDescriptionMap[executionId]
            ?: getResultFromAlfrescoAndConvertToTransformationDescription(executionId)
            ?: error("There is no transformation <$executionId> in progress")
        return if (transformationDescriptor.lock.tryLock(determinedWaitMax.toMillis(), MILLISECONDS)) {
            transformationDescriptor.result ?: throw transformationDescriptor.throwable
                ?: error("There is no result or throwable for transformation <$executionId>")
        } else {
            throw TimeoutException("Waiting time for transformation <$executionId> has expired")
        }
    }

    private fun getResultFromAlfrescoAndConvertToTransformationDescription(executionId: String): TransformationDescription? =
        ifPersistenceInAlfrescoIsSet {
            val lock = ReentrantLock()
            try {
                alfrescoPersistence.getResult(executionId)
                    ?.let { TransformationDescription(lock, result = it) }
                    .also { logger.debug { "Got transformation <$executionId> from Alfresco" } }
            } catch (e: Throwable) {
                TransformationDescription(lock, throwable = e)
                    .also { logger.debug { "Got transformation <$executionId> with error from Alfresco" } }
            }
        }

    private fun determineWaitMax(waitMax: Duration?): Duration =
        waitMax ?: this.waitMax

    override fun startTransformation(): TransformationExecution =
        runBlocking(dispatcher) {
            val executionId = UUID.randomUUID().toString()
            val transformationExecution = transformationExecution(executionId)

            val transformationDescription = TransformationDescription(ReentrantLock())
            transformationDescriptionMap[executionId] = transformationDescription
            try {
                transformationDescription.lock.lock()
                logger.debug { "Started transformation <$executionId>" }
                ifPersistenceInAlfrescoIsSet { alfrescoPersistence.startTransformation(transformationExecution) }
                transformationExecution
            } catch (e: Exception) {
                transformationDescriptionMap.remove(executionId)
                throw e
            }
        }

    override fun completeTransformation(transformationExecution: TransformationExecution, result: TransformationExecutionResult) {
        runBlocking(dispatcher) {
            val executionId = transformationExecution.id

            val inAlfresco = completeTransformationInAlfresco(transformationExecution, result)
            val inMemory = executeAndUnlock(executionId) { it.result = result }
            if (inAlfresco || inMemory) {
                logger.debug { "Completed transformation <$executionId>: ${result.nodeRefs}" }
            } else {
                logger.warn { "Couldn't complete transformation. There is no transformation <$executionId> in progress" }
            }
        }
    }

    private fun completeTransformationInAlfresco(transformationExecution: TransformationExecution, result: TransformationExecutionResult): Boolean =
        ifPersistenceInAlfrescoIsSet { alfrescoPersistence.completeTransformation(transformationExecution, result) } ?: false

    override fun completeErrorTransformation(transformationExecution: TransformationExecution, throwable: Throwable) {
        runBlocking(dispatcher) {
            val executionId = transformationExecution.id

            val inAlfresco = completeErrorTransformationInAlfresco(transformationExecution, throwable)
            val inMemory = executeAndUnlock(executionId) { it.throwable = throwable }
            if (inAlfresco || inMemory) {
                logger.debug(throwable) { "Completed <$executionId> transformation with error" }
            } else {
                logger.warn(throwable) { "Couldn't complete transformation with error. There is no <$executionId> transformation in progress" }
            }
        }
    }

    private fun completeErrorTransformationInAlfresco(transformationExecution: TransformationExecution, throwable: Throwable): Boolean =
        ifPersistenceInAlfrescoIsSet { alfrescoPersistence.completeErrorTransformation(transformationExecution, throwable) } ?: false

    private fun executeAndUnlock(executionId: String, toExecute: (TransformationDescription) -> Unit): Boolean {
        val transformationDescription = transformationDescriptionMap[executionId]

        return if (transformationDescription != null) {
            toExecute(transformationDescription)
            transformationDescription.lock.unlock()
            true
        } else {
            false
        }
    }

    private fun <T> ifPersistenceInAlfrescoIsSet(toRun: () -> T): T? =
        if (persistInAlfresco) {
            toRun()
        } else {
            null
        }
}