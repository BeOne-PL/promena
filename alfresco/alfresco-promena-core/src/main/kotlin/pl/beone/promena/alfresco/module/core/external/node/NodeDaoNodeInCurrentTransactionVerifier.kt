package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.repo.domain.node.NodeDAO
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PotentialConcurrentModificationException
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier

class NodeDaoNodeInCurrentTransactionVerifier(
    private val nodeDAO: NodeDAO
) : NodeInCurrentTransactionVerifier {

    override fun verify(nodeRef: NodeRef) {
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