package pl.beone.promena.module.http.client.contract.alfresco

import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName
import pl.beone.promena.module.http.client.applicationmodel.descriptor.NodeDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

interface AlfrescoTransformationService {

    fun transformToNodes(transformerId: String,
                         nodeDescriptors: List<NodeDescriptor>,
                         targetNodeDescriptors: List<NodeDescriptor>,
                         targetMediaType: MediaType,
                         parameters: Parameters,
                         timeout: Long?)

    fun transformToFolder(transformerId: String,
                          nodeDescriptors: List<NodeDescriptor>,
                          targetFolderNodeRef: NodeRef,
                          targetMediaType: MediaType,
                          parameters: Parameters,
                          timeout: Long?,
                          targetType: QName?,
                          targetContentProperty: QName?,
                          namePattern: String?): List<NodeRef>
}