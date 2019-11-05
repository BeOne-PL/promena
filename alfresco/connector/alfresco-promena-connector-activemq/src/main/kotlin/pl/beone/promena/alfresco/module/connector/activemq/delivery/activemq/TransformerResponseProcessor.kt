package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.node.NodesExistenceVerifier
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.extension.couldNotTransform
import pl.beone.promena.alfresco.module.core.extension.stoppedTransformingBecauseChecksumsAreDifferent
import pl.beone.promena.alfresco.module.core.extension.stoppedTransformingBecauseNodeDoesNotExist
import pl.beone.promena.transformer.contract.transformation.Transformation

class TransformerResponseProcessor(
    private val promenaMutableTransformationManager: PromenaMutableTransformationManager,
    private val nodesExistenceVerifier: NodesExistenceVerifier,
    private val nodesChecksumGenerator: NodesChecksumGenerator,
    private val authorizationService: AuthorizationService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun process(
        transformation: Transformation,
        nodeDescriptor: NodeDescriptor,
        transformationExecution: TransformationExecution,
        nodesChecksum: String,
        userName: String,
        toRunIfNodesExistAndHaveTheSameChecksum: () -> Unit
    ) {
        try {
            val nodeRefs = nodeDescriptor.toNodeRefs()
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
}