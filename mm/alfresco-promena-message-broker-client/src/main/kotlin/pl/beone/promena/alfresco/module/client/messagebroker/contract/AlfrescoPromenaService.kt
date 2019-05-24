package pl.beone.promena.alfresco.module.client.messagebroker.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

interface AlfrescoPromenaService {

    fun transform(transformerId: String,
                  nodeRefs: List<NodeRef>,
                  targetMediaType: MediaType,
                  parameters: Parameters?,
                  waitMax: Duration? = null): List<NodeRef>

    fun transformAsync(transformerId: String,
                       nodeRefs: List<NodeRef>,
                       targetMediaType: MediaType,
                       parameters: Parameters?)

}