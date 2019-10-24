package pl.beone.promena.alfresco.module.core.external.node

import io.kotlintest.shouldNotThrowExactly
import io.kotlintest.shouldThrowExactly
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT

@RunWith(AlfrescoTestRunner::class)
class DefaultNodesExistenceVerifierTestIT : AbstractUtilsAlfrescoIT() {

    private lateinit var nodesExistenceVerifier: DefaultNodesExistenceVerifier

    @Before
    fun setUp() {
        nodesExistenceVerifier = DefaultNodesExistenceVerifier(serviceRegistry)
    }

    @Test
    fun verify() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()

        shouldNotThrowExactly<InvalidNodeRefException> {
            nodesExistenceVerifier.verify(listOf(serviceRegistry.nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE), nodeRef))
        }
    }

    @Test
    fun verify_nodeDoesNotExist() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()

        shouldThrowExactly<InvalidNodeRefException> {
            nodesExistenceVerifier.verify(listOf(nodeRef, NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")))
        }
    }
}