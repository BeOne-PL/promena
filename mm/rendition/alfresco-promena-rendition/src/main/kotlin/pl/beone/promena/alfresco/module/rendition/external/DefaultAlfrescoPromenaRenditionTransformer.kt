package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_PREFIX
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionTransformer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration

class DefaultAlfrescoPromenaRenditionTransformer(
    private val alfrescoRenditionGetter: AlfrescoRenditionGetter,
    private val alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter,
    private val alfrescoPromenaTransformer: AlfrescoPromenaTransformer,
    private val timeout: Duration
) : AlfrescoPromenaRenditionTransformer {

    companion object {
        private val logger = KotlinLogging.logger {}

        private val metadataRenditionNameProperty = "alf_$PROMENA_MODEL_1_0_PREFIX:${PROP_RENDITION_NAME.localName}"
    }

    override fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef? {
        logger.debug { "Performing <$renditionName> rendition sync transformation of <$nodeRef>..." }

        return try {
            alfrescoPromenaTransformer.transform(
                getTransformation(renditionName),
                createNodeRefWithMetadataRenditionProperty(nodeRef, renditionName),
                timeout
            )

            logger.debug { "Finished performing <$renditionName> rendition sync transformation of <$nodeRef>" }

            alfrescoRenditionGetter.getRendition(nodeRef, renditionName)
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logger.warn(e) { "Couldn't perform <$renditionName> rendition sync transformation of <$nodeRef>. Skipped" }
            null
        } catch (e: Exception) {
            logger.error(e) { "Couldn't perform <$renditionName> rendition sync transformation of <$nodeRef>" }
            null
        }
    }

    override fun transformAsync(nodeRef: NodeRef, renditionName: String) {
        logger.debug { "Performing <$renditionName> rendition async transformation of <$nodeRef>..." }

        try {
            alfrescoPromenaTransformer.transformAsync(getTransformation(renditionName), createNodeRefWithMetadataRenditionProperty(nodeRef, renditionName))
                .subscribe(
                    { logger.debug { "Finished performing <$renditionName> rendition async transformation of <$nodeRef>" } },
                    { exception -> logger.error(exception) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>" } }
                )
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logger.warn(e) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>. Skipped" }
        }
    }

    private fun getTransformation(renditionName: String): Transformation =
        alfrescoPromenaRenditionDefinitionGetter.getByRenditionName(renditionName).getTransformation()

    private fun createNodeRefWithMetadataRenditionProperty(nodeRef: NodeRef, renditionName: String): List<NodeDescriptor> =
        listOf(
            nodeRef.toNodeDescriptor(emptyMetadata() + (metadataRenditionNameProperty to renditionName))
        )
}
