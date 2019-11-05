package pl.beone.promena.alfresco.module.connector.http.external

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.*
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorInjector
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.alfresco.module.core.extension.*
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.lib.connector.http.external.HttpPromenaTransformer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.util.concurrent.Executors

class HttpPromenaTransformationExecutor(
    threads: Int,
    private val httpAddress: String,
    private val externalCommunicationParameters: CommunicationParameters,
    private val promenaMutableTransformationManager: PromenaTransformationManager.PromenaMutableTransformationManager,
    private val retry: Retry,
    private val postTransformationExecutorValidator: PostTransformationExecutorValidator,
    private val nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier,
    private val nodesChecksumGenerator: NodesChecksumGenerator,
    private val nodesExistenceVerifier: NodesExistenceVerifier,
    private val dataDescriptorGetter: DataDescriptorGetter,
    private val transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
    private val postTransformationExecutorInjector: PostTransformationExecutorInjector,
    private val authorizationService: AuthorizationService,
    private val serviceRegistry: ServiceRegistry,
    serializationService: SerializationService
) : PromenaTransformationExecutor {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val httpPromenaTransformer = HttpPromenaTransformer(serializationService)
    private val coroutineScope = CoroutineScope(Executors.newFixedThreadPool(threads).asCoroutineDispatcher())

    override fun execute(
        transformation: Transformation,
        nodeDescriptor: NodeDescriptor,
        postTransformationExecutor: PostTransformationExecutor?,
        retry: Retry?
    ): TransformationExecution {
        val nodeRefs = nodeDescriptor.toNodeRefs()

        postTransformationExecutor?.let(postTransformationExecutorValidator::validate)
        nodeRefs.forEach(nodeInCurrentTransactionVerifier::verify)

        val nodesChecksum = nodesChecksumGenerator.generate(nodeRefs)
        val userName = authorizationService.getCurrentUser()

        val dataDescriptor = dataDescriptorGetter.get(nodeDescriptor)

        val transformationExecution = promenaMutableTransformationManager.startTransformation()

        logger.start(transformation, nodeDescriptor)

        coroutineScope.launch {
            transform(
                Parameters(
                    transformation,
                    nodeDescriptor,
                    postTransformationExecutor,
                    determineRetry(retry),
                    dataDescriptor,
                    transformationExecution,
                    nodesChecksum,
                    userName
                ),
                0
            )
        }

        return transformationExecution
    }

    private fun determineRetry(retry: Retry?): Retry =
        retry ?: this.retry

    private suspend fun transform(parameters: Parameters, attempt: Long) {
        val (transformation, _, _, _, dataDescriptor, _, _, _) = parameters

        try {
            val startTimestamp = getTimestamp()
            val performedTransformationDescriptor =
                httpPromenaTransformer.execute(transformationDescriptor(transformation, dataDescriptor, externalCommunicationParameters), httpAddress)
            val endTimestamp = getTimestamp()

            process(parameters) {
                processTransformedData(parameters, performedTransformationDescriptor, startTimestamp, endTimestamp)
            }
        } catch (e: Exception) {
            process(parameters) {
                processException(parameters, attempt, e)
            }
        }
    }

    private suspend fun process(parameters: Parameters, toRunIfNodesExistAndHaveTheSameChecksum: suspend () -> Unit) {
        val (transformation, nodeDescriptor, _, _, _, transformationExecution, nodesChecksum, userName) = parameters
        val nodeRefs = nodeDescriptor.toNodeRefs()

        try {
            authorizationService.runAs(userName) { nodesExistenceVerifier.verify(nodeRefs) }

            val currentNodesChecksum = authorizationService.runAs(userName) { nodesChecksumGenerator.generate(nodeRefs) }
            if (nodesChecksum != currentNodesChecksum) {
                logger.stoppedTransformingBecauseChecksumsAreDifferent(transformation, nodeDescriptor, nodesChecksum, currentNodesChecksum)
                promenaMutableTransformationManager.completeErrorTransformation(
                    transformationExecution,
                    NodesInconsistencyException(nodeRefs, nodesChecksum, currentNodesChecksum)
                )
            } else {
                toRunIfNodesExistAndHaveTheSameChecksum()
            }
        } catch (e: InvalidNodeRefException) {
            logger.stoppedTransformingBecauseNodeDoesNotExist(transformation, nodeDescriptor, e.nodeRef)
            promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, e)
        } catch (e: Exception) {
            logger.couldNotTransform(transformation, nodeDescriptor, e)
            promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, e)
        }
    }

    private fun processTransformedData(
        parameters: Parameters,
        performedTransformationDescriptor: PerformedTransformationDescriptor,
        startTimestamp: Long,
        endTimestamp: Long
    ) {
        val (transformation, nodeDescriptor, postTransformationExecutor, _, _, transformationExecution, _, userName) = parameters

        val transformationExecutionResult = authorizationService.runAs(userName) {
            serviceRegistry.retryingTransactionHelper.doInTransaction({
                transformedDataDescriptorSaver.save(
                    transformation,
                    nodeDescriptor.toNodeRefs(),
                    performedTransformationDescriptor.transformedDataDescriptor
                )
                    .let(::transformationExecutionResult)
                    .also { result ->
                        postTransformationExecutor
                            ?.also(postTransformationExecutorInjector::inject)
                            ?.execute(transformation, nodeDescriptor, result)
                    }
            }, false, true)
        }

        logger.transformedSuccessfully(transformation, nodeDescriptor, transformationExecutionResult, startTimestamp, endTimestamp)
        promenaMutableTransformationManager.completeTransformation(transformationExecution, transformationExecutionResult)
    }

    private suspend fun processException(parameters: Parameters, attempt: Long, exception: Exception) {
        val (transformation, nodeDescriptor, _, retry, _, transformationExecution, _, _) = parameters

        if (retry is Retry.No || wasLastAttempt(attempt, retry.maxAttempts)) {
            logger.couldNotTransform(transformation, nodeDescriptor, exception)
            promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, exception)
        } else {
            val currentAttempt = attempt + 1

            logger.logOnRetry(transformation, nodeDescriptor, currentAttempt, retry.maxAttempts, retry.nextAttemptDelay, exception)
            delay(retry.nextAttemptDelay.toMillis())

            transform(parameters, currentAttempt)
        }
    }

    private fun wasLastAttempt(attempt: Long, retryMaxAttempts: Long): Boolean =
        attempt == retryMaxAttempts

    private fun getTimestamp(): Long =
        System.currentTimeMillis()
}
