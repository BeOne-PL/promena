package pl.beone.promena.alfresco.module.rendition.external.rendition

import org.alfresco.model.RenditionModel.ASPECT_RENDITION
import org.alfresco.repo.rendition.CompositeRenditionDefinitionImpl
import org.alfresco.repo.rendition.RenderingEngineDefinitionImpl
import org.alfresco.repo.rendition.RenditionDefinitionImpl
import org.alfresco.service.cmr.rendition.*
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.QName
import org.alfresco.util.GUID
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionTransformationExecutor
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter

class PromenaRenditionService(
    private val nodeService: NodeService,
    private val renditionGetter: RenditionGetter,
    private val promenaRenditionTransformationExecutor: PromenaRenditionTransformationExecutor
) : RenditionService {

    override fun createCompositeRenditionDefinition(renditionName: QName?): CompositeRenditionDefinition =
        CompositeRenditionDefinitionImpl(GUID.generate(), renditionName)

    override fun getRenderingEngineDefinition(name: String?): RenderingEngineDefinition =
        RenderingEngineDefinitionImpl(name)

    override fun isRendition(node: NodeRef): Boolean =
        nodeService.hasAspect(node, ASPECT_RENDITION)

    override fun getRenderingEngineDefinitions(): List<RenderingEngineDefinition> =
        emptyList()

    override fun getRenditions(node: NodeRef): List<ChildAssociationRef> =
        renditionGetter.getRenditions(node)

    override fun getRenditions(node: NodeRef, mimeTypePrefix: String?): List<ChildAssociationRef> =
        renditionGetter.getRenditions(node)

    override fun loadRenditionDefinition(renditionName: QName): RenditionDefinition =
        RenditionDefinitionImpl(GUID.generate(), renditionName, renditionName.localName)

    override fun usingRenditionService2(sourceNodeRef: NodeRef?, rendDefn: RenditionDefinition?): Boolean =
        true

    override fun saveRenditionDefinition(renditionDefinition: RenditionDefinition?) {
        // deliberately omitted
    }

    override fun createRenditionDefinition(renditionName: QName?, renderingEngineName: String?): RenditionDefinition =
        RenditionDefinitionImpl(GUID.generate(), renditionName, renderingEngineName)

    override fun getRenditionByName(node: NodeRef, renditionName: QName): ChildAssociationRef? =
        renditionGetter.getRendition(node, renditionName.localName)

    override fun getSourceNode(renditionNode: NodeRef?): ChildAssociationRef =
        TODO("not implemented")

    override fun render(sourceNode: NodeRef, renditionDefinition: RenditionDefinition): ChildAssociationRef =
        promenaRenditionTransformationExecutor.transform(sourceNode, renditionDefinition.renditionName.localName)

    override fun render(sourceNode: NodeRef, renditionDefinition: RenditionDefinition, callback: RenderCallback?) {
        promenaRenditionTransformationExecutor.transformAsync(sourceNode, renditionDefinition.renditionName.localName)
    }

    override fun render(sourceNode: NodeRef, renditionDefinitionQName: QName): ChildAssociationRef =
        promenaRenditionTransformationExecutor.transform(sourceNode, renditionDefinitionQName.localName)

    override fun render(sourceNode: NodeRef, renditionDefinitionQName: QName, callback: RenderCallback?) {
        promenaRenditionTransformationExecutor.transformAsync(sourceNode, renditionDefinitionQName.localName)
    }

    override fun loadRenditionDefinitions(): List<RenditionDefinition> =
        emptyList()

    override fun loadRenditionDefinitions(renderingEngineName: String?): List<RenditionDefinition> =
        emptyList()

    override fun cancelRenditions(sourceNode: NodeRef?) {
        // deliberately omitted
    }

    override fun cancelRenditions(sourceNode: NodeRef?, type: String?) {
        // deliberately omitted
    }
}