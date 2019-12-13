package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import mu.KotlinLogging
import org.alfresco.service.ServiceRegistry
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.TransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorInjector
import pl.beone.promena.alfresco.module.core.extension.transformedSuccessfully
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor

class TransformerResponseConsumer(
    private val promenaMutableTransformationManager: PromenaMutableTransformationManager,
    private val transformerResponseProcessor: TransformerResponseProcessor,
    private val transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
    private val transformationParametersSerializationService: TransformationParametersSerializationService,
    private val postTransformationExecutorInjector: PostTransformationExecutorInjector,
    private val authorizationService: AuthorizationService,
    private val serviceRegistry: ServiceRegistry
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @JmsListener(destination = "\${promena.connector.activemq.consumer.queue.response}")
    fun receiveQueue(
        @Header(CORRELATION_ID) executionId: String,
        @Header(TRANSFORMATION_START_TIMESTAMP) startTimestamp: Long,
        @Header(TRANSFORMATION_END_TIMESTAMP) endTimestamp: Long,
        @Header(SEND_BACK_TRANSFORMATION_PARAMETERS) transformationParameters: String,
        @Payload performedTransformationDescriptor: PerformedTransformationDescriptor
    ) {
        val transformationExecution = transformationExecution(executionId)

        val (transformation, nodeDescriptor, postTransformationExecutor, _, dataDescriptor, nodesChecksum, _, userName) =
            transformationParametersSerializationService.deserialize(transformationParameters)
        val nodeRefs = nodeDescriptor.toNodeRefs()

        transformerResponseProcessor.process(transformation, nodeDescriptor, transformationExecution, nodesChecksum, userName) {
            val transformationExecutionResult = authorizationService.runAs(userName) {
                serviceRegistry.retryingTransactionHelper.doInTransaction({
                    transformedDataDescriptorSaver.save(executionId, transformation, nodeRefs, performedTransformationDescriptor.transformedDataDescriptor)
                        .let(::transformationExecutionResult)
                        .also { result ->
                            postTransformationExecutor
                                ?.also(postTransformationExecutorInjector::inject)
                                ?.execute(transformation, nodeDescriptor, result)

                            // TODO
                            dataDescriptor.descriptors.forEach { it.data.delete() }
                        }
                }, false, true)
            }

            logger.transformedSuccessfully(transformation, nodeDescriptor, transformationExecutionResult, startTimestamp, endTimestamp)
            promenaMutableTransformationManager.completeTransformation(transformationExecution, transformationExecutionResult)
        }
    }
}