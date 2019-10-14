package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.contract.PromenaTransformer
import pl.beone.promena.alfresco.module.core.external.MinimalRenditionTransformedDataDescriptorSaver.Companion.METADATA_ALF_PREFIX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_PREFIX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionTransformer
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter
import pl.beone.promena.alfresco.module.rendition.extension.getMediaType
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration

class DefaultPromenaRenditionTransformer(
    private val contentService: ContentService,
    private val renditionGetter: RenditionGetter,
    private val promenaRenditionDefinitionGetter: PromenaRenditionDefinitionGetter,
    private val promenaRenditionInProgressSynchronizer: PromenaRenditionInProgressSynchronizer,
    private val promenaTransformer: PromenaTransformer,
    private val timeout: Duration
) : PromenaRenditionTransformer {

    companion object {
        private val logger = KotlinLogging.logger {}

        val METADATA_RENDITION_NAME_PROPERTY = METADATA_ALF_PREFIX + PROMENA_MODEL_1_0_PREFIX + ":" + PROP_RENDITION_NAME.localName
    }

    override fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef {
        logger.debug { "Performing <$renditionName> rendition sync transformation of <$nodeRef>..." }

        promenaRenditionInProgressSynchronizer.isInProgress(nodeRef, renditionName)
        val transformation = getTransformation(nodeRef, renditionName)
        try {
            promenaRenditionInProgressSynchronizer.start(nodeRef, renditionName)

            promenaTransformer.transform(transformation, createNodeRefWithMetadataRenditionProperty(nodeRef, renditionName), timeout)

            logger.debug { "Finished performing <$renditionName> rendition sync transformation of <$nodeRef>" }
        } catch (e: Exception) {
            logger.error(e) { "Couldn't perform <$renditionName> rendition sync transformation of <$nodeRef>" }
            throw e
        } finally {
            promenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName)
        }

        return renditionGetter.getRendition(nodeRef, renditionName)
            ?: throw NoSuchElementException("There is no <$renditionName> rendition of <$nodeRef>")
    }

    override fun transformAsync(nodeRef: NodeRef, renditionName: String) {
        logger.debug { "Performing <$renditionName> rendition async transformation of <$nodeRef>..." }

        promenaRenditionInProgressSynchronizer.isInProgress(nodeRef, renditionName)
        val transformation = getTransformation(nodeRef, renditionName)
        try {
            promenaRenditionInProgressSynchronizer.start(nodeRef, renditionName)

            promenaTransformer.transformAsync(transformation, createNodeRefWithMetadataRenditionProperty(nodeRef, renditionName))
                .subscribe(
                    { logger.debug { "Finished performing <$renditionName> rendition async transformation of <$nodeRef>" } },
                    { e -> logger.error(e) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>" } },
                    { promenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName) }
                )
        } catch (e: Exception) {
            promenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName)

            logger.warn(e) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>" }
            throw e
        }
    }

    private fun getTransformation(nodeRef: NodeRef, renditionName: String): Transformation =
        promenaRenditionDefinitionGetter
            .getByRenditionName(renditionName)
            .getTransformation(contentService.getMediaType(nodeRef))

    private fun createNodeRefWithMetadataRenditionProperty(nodeRef: NodeRef, renditionName: String): List<NodeDescriptor> =
        listOf(nodeRef.toNodeDescriptor(createMetadata(renditionName)))

    private fun createMetadata(renditionName: String): MapMetadata =
        emptyMetadata() +
                (METADATA_RENDITION_NAME_PROPERTY to renditionName)
}
