package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.repo.domain.node.NodeDAO
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PotentialConcurrentModificationException

class NodeInCurrentTransactionVerifier(
    private val nodeDAO: NodeDAO
) {

    @Throws(PotentialConcurrentModificationException::class)
    fun verify(nodeRef: NodeRef) {
        val isInCurrentTransaction = try {
            getDbId(nodeRef)?.let(nodeDAO::isInCurrentTxn) ?: false
        } catch (e: Exception) {
            false
        }

        if (isInCurrentTransaction) {
            throw PotentialConcurrentModificationException(nodeRef)
        }
    }

    private fun getDbId(nodeRef: NodeRef): Long? =
        nodeDAO.getNodePair(nodeRef)?.first
}