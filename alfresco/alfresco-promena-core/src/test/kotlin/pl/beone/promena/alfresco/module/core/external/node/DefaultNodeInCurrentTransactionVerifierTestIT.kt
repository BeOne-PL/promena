package pl.beone.promena.alfresco.module.core.external.node

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowExactly
import io.kotlintest.shouldThrowExactly
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.repo.domain.node.NodeDAO
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT

@RunWith(AlfrescoTestRunner::class)
class DefaultNodeInCurrentTransactionVerifierTestIT : AbstractUtilsAlfrescoIT() {

    private lateinit var nodeInCurrentTransactionVerifier: DefaultNodeInCurrentTransactionVerifier

    @Before
    fun setUp() {
        nodeInCurrentTransactionVerifier = DefaultNodeInCurrentTransactionVerifier(applicationContext.getBean("nodeDAO", NodeDAO::class.java))
    }

    @Test
    fun verify_anExistingUnchangedNode() {
        shouldNotThrowExactly<ConcurrentModificationException> {
            nodeInCurrentTransactionVerifier.verify(serviceRegistry.nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE))
        }
    }

    @Test
    fun verify_aNodeWasCreatedInANewTransactionThatWasFinishedBeforeVerification() {
        serviceRegistry.retryingTransactionHelper.doInTransaction({
            createOrGetIntegrationTestsFolder().createNode()
        }, false, true)

        shouldNotThrowExactly<ConcurrentModificationException> {
            nodeInCurrentTransactionVerifier.verify(serviceRegistry.nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE))
        }
    }

    @Test
    fun verify_aNodeIsCreatedInTheSameTransaction_shouldThrowConcurrentModificationException() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()

        shouldThrowExactly<ConcurrentModificationException> {
            nodeInCurrentTransactionVerifier.verify(nodeRef)
        }.message shouldBe "Node <$nodeRef> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Complete this transaction before executing the transformation"
    }

    // It should pass but the nature of Alfresco IT test causes an error (node doesn't exist in the second transaction)
//    @Test
//    fun verify_nameNodePropertyIsChangedInTheSameTransaction_shouldThrowConcurrentModificationException() {
//        val nodeRef = serviceRegistry.retryingTransactionHelper.doInTransaction({
//            createOrGetIntegrationTestsFolder().createNode()
//        }, false, true)
//
//        serviceRegistry.retryingTransactionHelper.doInTransaction({
//            serviceRegistry.nodeService.setProperty(nodeRef, PROP_NAME, "changed")
//
//            shouldThrowExactly<ConcurrentModificationException> {
//                nodeInCurrentTransactionVerifier.verify(nodeRef)
//            }.message shouldBe "Node <$nodeRef> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Complete this transaction before executing the transformation"
//        }, false, true)
//    }
}