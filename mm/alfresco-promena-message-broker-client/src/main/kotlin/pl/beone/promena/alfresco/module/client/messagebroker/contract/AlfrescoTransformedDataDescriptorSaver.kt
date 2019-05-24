package pl.beone.promena.alfresco.module.client.messagebroker.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface AlfrescoTransformedDataDescriptorSaver {

    fun save(transformerId: String,
             nodeRefs: List<NodeRef>,
             targetMediaType: MediaType,
             transformedDataDescriptors: List<TransformedDataDescriptor>) : List<NodeRef>
}