package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.repo.domain.node.NodeDAO
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier

class DefaultNodeInCurrentTransactionVerifier(
    private val nodeDAO: NodeDAO
) : NodeInCurrentTransactionVerifier {

    override fun verify(nodeRef: NodeRef) {
        val isInCurrentTransaction = try {
            getDbId(nodeRef)?.let(nodeDAO::isInCurrentTxn) ?: false
        } catch (e: Exception) {
            false
        }

        if (isInCurrentTransaction) {
            throw ConcurrentModificationException("Node <$nodeRef> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Complete this transaction before executing the transformation")
        }
    }

    private fun getDbId(nodeRef: NodeRef): Long? =
        nodeDAO.getNodePair(nodeRef)?.first
}