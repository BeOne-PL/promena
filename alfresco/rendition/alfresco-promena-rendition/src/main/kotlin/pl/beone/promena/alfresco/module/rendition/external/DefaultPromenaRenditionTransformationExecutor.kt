package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionTransformationExecutor
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter
import pl.beone.promena.alfresco.module.rendition.contract.definition.PromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.extension.getMediaType
import pl.beone.promena.alfresco.module.rendition.internal.transformation.definition.PromenaRenditionNamePromenaTransformationMetadataMappingDefinition.Companion.PROP_RENDITION_NAME_PREFIXED
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration

class DefaultPromenaRenditionTransformationExecutor(
    private val serviceRegistry: ServiceRegistry,
    private val renditionGetter: RenditionGetter,
    private val promenaRenditionDefinitionGetter: PromenaRenditionDefinitionGetter,
    private val promenaRenditionInProgressSynchronizer: PromenaRenditionInProgressSynchronizer,
    private val promenaTransformationExecutor: PromenaTransformationExecutor,
    private val promenaTransformationManager: PromenaTransformationManager,
    private val timeout: Duration
) : PromenaRenditionTransformationExecutor {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef =
        transform(nodeRef, renditionName) { transformationExecution ->
            promenaTransformationManager.getResult(transformationExecution, timeout)

            renditionGetter.getRendition(nodeRef, renditionName) ?: throw NoSuchElementException("There is no <$renditionName> rendition of <$nodeRef>")
        }

    override fun transformAsync(nodeRef: NodeRef, renditionName: String) {
        transform(nodeRef, renditionName) {}
    }

    private fun <T> transform(nodeRef: NodeRef, renditionName: String, toRun: (TransformationExecution) -> T): T {
        logger.debug { "Transforming <$renditionName> rendition of <$nodeRef>..." }

        promenaRenditionInProgressSynchronizer.isInProgress(nodeRef, renditionName)
        val transformation = getTransformation(nodeRef, renditionName)
        return try {
            promenaRenditionInProgressSynchronizer.start(nodeRef, renditionName)

            val transformationExecution = promenaTransformationExecutor.execute(
                transformation,
                creatRenditionNodeDescriptor(nodeRef, renditionName),
                FinishPostTransformationExecutor(promenaRenditionInProgressSynchronizer, renditionName)
            )

            toRun(transformationExecution)
        } catch (e: Exception) {
            promenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName)

            logger.error(e) { "Couldn't transform <$renditionName> rendition of <$nodeRef>" }
            throw e
        }
    }

    private fun getTransformation(nodeRef: NodeRef, renditionName: String): Transformation =
        promenaRenditionDefinitionGetter
            .getByRenditionName(renditionName)
            .getTransformation(serviceRegistry.contentService.getMediaType(nodeRef))

    private fun creatRenditionNodeDescriptor(nodeRef: NodeRef, renditionName: String): NodeDescriptor =
        nodeRef.toSingleNodeDescriptor(emptyMetadata() + (PROP_RENDITION_NAME_PREFIXED to renditionName))

    internal class FinishPostTransformationExecutor(
        private val promenaRenditionInProgressSynchronizer: PromenaRenditionInProgressSynchronizer,
        private val renditionName: String
    ) : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
            val nodeRef = nodeDescriptor.descriptors[0].nodeRef

            promenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName)

            KotlinLogging.logger {}.debug { "Transformed <$renditionName> rendition of <$nodeRef>" }
        }
    }
}
