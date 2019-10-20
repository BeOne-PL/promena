package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PotentialConcurrentModificationException

interface NodeInCurrentTransactionVerifier {

    @Throws(PotentialConcurrentModificationException::class)
    fun verify(nodeRef: NodeRef)
}