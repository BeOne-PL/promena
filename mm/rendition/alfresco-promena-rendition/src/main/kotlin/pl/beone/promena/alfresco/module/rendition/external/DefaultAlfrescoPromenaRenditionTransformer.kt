package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_PREFIX
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionTransformer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter
import pl.beone.promena.alfresco.module.rendition.extension.getMediaType
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration

class DefaultAlfrescoPromenaRenditionTransformer(
    private val contentService: ContentService,
    private val alfrescoRenditionGetter: AlfrescoRenditionGetter,
    private val alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter,
    private val alfrescoPromenaRenditionInProgressSynchronizer: AlfrescoPromenaRenditionInProgressSynchronizer,
    private val alfrescoPromenaTransformer: AlfrescoPromenaTransformer,
    private val timeout: Duration
) : AlfrescoPromenaRenditionTransformer {

    companion object {
        private val logger = KotlinLogging.logger {}

        private val metadataRenditionNameProperty = "alf_$PROMENA_MODEL_1_0_PREFIX:${PROP_RENDITION_NAME.localName}"
    }

    override fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef {
        logger.debug { "Performing <$renditionName> rendition sync transformation of <$nodeRef>..." }

        alfrescoPromenaRenditionInProgressSynchronizer.isInProgress(nodeRef, renditionName)
        val transformation = getTransformation(nodeRef, renditionName)
        try {
            alfrescoPromenaRenditionInProgressSynchronizer.start(nodeRef, renditionName)

            alfrescoPromenaTransformer.transform(transformation, createNodeRefWithMetadataRenditionProperty(nodeRef, renditionName), timeout)

            logger.debug { "Finished performing <$renditionName> rendition sync transformation of <$nodeRef>" }
        } catch (e: Exception) {
            logger.error(e) { "Couldn't perform <$renditionName> rendition sync transformation of <$nodeRef>" }
            throw e
        } finally {
            alfrescoPromenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName)
        }

        return alfrescoRenditionGetter.getRendition(nodeRef, renditionName)
            ?: throw NoSuchElementException("There is no <$renditionName> rendition of <$nodeRef>")
    }

    override fun transformAsync(nodeRef: NodeRef, renditionName: String) {
        logger.debug { "Performing <$renditionName> rendition async transformation of <$nodeRef>..." }

        alfrescoPromenaRenditionInProgressSynchronizer.isInProgress(nodeRef, renditionName)
        val transformation = getTransformation(nodeRef, renditionName)
        try {
            alfrescoPromenaRenditionInProgressSynchronizer.start(nodeRef, renditionName)

            alfrescoPromenaTransformer.transformAsync(transformation, createNodeRefWithMetadataRenditionProperty(nodeRef, renditionName))
                .subscribe(
                    { logger.debug { "Finished performing <$renditionName> rendition async transformation of <$nodeRef>" } },
                    { e -> logger.error(e) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>" } },
                    { alfrescoPromenaRenditionInProgressSynchronizer.finish(nodeRef, renditionName) }
                )
        } catch (e: Exception) {
            logger.warn(e) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>" }
            throw e
        }
    }

    private fun getTransformation(nodeRef: NodeRef, renditionName: String): Transformation =
        alfrescoPromenaRenditionDefinitionGetter.getByRenditionName(renditionName).getTransformation(nodeRef, contentService.getMediaType(nodeRef))

    private fun createNodeRefWithMetadataRenditionProperty(nodeRef: NodeRef, renditionName: String): List<NodeDescriptor> =
        listOf(
            nodeRef.toNodeDescriptor(emptyMetadata() + (metadataRenditionNameProperty to renditionName))
        )
}
