package pl.beone.promena.alfresco.module.client.base.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

interface AlfrescoTransformedDataDescriptorSaver {

    fun save(transformerId: String,
             nodeRefs: List<NodeRef>,
             targetMediaType: MediaType,
             transformedDataDescriptors: TransformedDataDescriptor): List<NodeRef>
}