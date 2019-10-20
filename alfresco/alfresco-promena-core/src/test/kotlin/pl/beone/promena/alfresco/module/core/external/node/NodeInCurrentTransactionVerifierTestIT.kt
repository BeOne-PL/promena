package pl.beone.promena.alfresco.module.core.external.node

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowExactly
import io.kotlintest.shouldThrowExactly
import org.alfresco.model.ContentModel.PROP_NAME
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.repo.domain.node.NodeDAO
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PotentialConcurrentModificationException
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT

@RunWith(AlfrescoTestRunner::class)
class NodeInCurrentTransactionVerifierTestIT : AbstractUtilsAlfrescoIT() {

    private val nodeInCurrentTransactionVerifier = NodeInCurrentTransactionVerifier(
        applicationContext.getBean("nodeDAO", NodeDAO::class.java)
    )

    @Test
    fun verify_anExistingUnchangedNode() {
        shouldNotThrowExactly<PotentialConcurrentModificationException> {
            nodeInCurrentTransactionVerifier.verify(serviceRegistry.nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE))
        }
    }

    @Test
    fun verify_aNodeWasCreatedInANewTransactionThatWasFinishedBeforeVerification() {
        val nodeRef = serviceRegistry.retryingTransactionHelper.doInTransaction({
            createOrGetIntegrationTestsFolder().createNode()
        }, false, true)

        shouldNotThrowExactly<PotentialConcurrentModificationException> {
            nodeInCurrentTransactionVerifier.verify(serviceRegistry.nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE))
        }
    }

    @Test
    fun verify_aNodeIsCreatedInTheSameTransaction_shouldThrowPotentialConcurrentModificationException() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()

        shouldThrowExactly<PotentialConcurrentModificationException> {
            nodeInCurrentTransactionVerifier.verify(nodeRef)
        }.message shouldBe "Node <$nodeRef> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Finish this transaction before a transformation"
    }

    // It should pass but the nature of Alfresco IT test causes an error (node doesn't exist in the second transaction)
//    @Test
//    fun verify_nameNodePropertyIsChangedInTheSameTransaction_shouldThrowPotentialConcurrentModificationException() {
//        val nodeRef = serviceRegistry.retryingTransactionHelper.doInTransaction({
//            createOrGetIntegrationTestsFolder().createNode()
//        }, false, true)
//
//        serviceRegistry.retryingTransactionHelper.doInTransaction({
//            serviceRegistry.nodeService.setProperty(nodeRef, PROP_NAME, "changed")
//
//            shouldThrowExactly<PotentialConcurrentModificationException> {
//                nodeInCurrentTransactionVerifier.verify(nodeRef)
//            }.message shouldBe "Node <$nodeRef> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Finish this transaction before a transformation"
//        }, false, true)
//    }
}