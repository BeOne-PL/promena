package pl.beone.promena.alfresco.module.rendition.external

import io.kotlintest.shouldBe
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter

@RunWith(AlfrescoTestRunner::class)
class PromenaAlfrescoRenditionGetterTest : AbstractUtilsAlfrescoIT() {

    private lateinit var nodeRef: NodeRef
    private lateinit var nodeRefDoclib: ChildAssociationRef
    private lateinit var nodeRefDoclib2: ChildAssociationRef
    private lateinit var nodeRefPdf: ChildAssociationRef

    private lateinit var nodeRef2: NodeRef
    private lateinit var nodeRef2Pdf: ChildAssociationRef

    private lateinit var alfrescoRenditionGetter: AlfrescoRenditionGetter

    @Before
    fun setUp() {
        val integrationTestsFolder = createOrGetIntegrationTestsFolder()

        nodeRef = integrationTestsFolder.createNode()
        nodeRefDoclib = nodeRef.createRenditionNode().setRenditionName("doclib")
        nodeRefDoclib2 = nodeRef.createRenditionNode().setRenditionName("doclib")
        nodeRef.createRenditionNode()
        nodeRefPdf = nodeRef.createRenditionNode().setRenditionName("pdf")

        nodeRef2 = integrationTestsFolder.createNode()
        nodeRef2Pdf = nodeRef2.createRenditionNode().setRenditionName("pdf")

        alfrescoRenditionGetter = PromenaAlfrescoRenditionGetter(serviceRegistry.nodeService)
    }

    @Test
    fun getRenditions() {
        alfrescoRenditionGetter.getRenditions(nodeRef) shouldBe
                listOf(nodeRefDoclib, nodeRefDoclib2, nodeRefPdf)

        alfrescoRenditionGetter.getRenditions(nodeRef2) shouldBe
                listOf(nodeRef2Pdf)
    }

    @Test
    fun getRendition() {
        alfrescoRenditionGetter.getRendition(nodeRef, "doclib") shouldBe nodeRefDoclib2
        alfrescoRenditionGetter.getRendition(nodeRef, "pdf") shouldBe nodeRefPdf

        alfrescoRenditionGetter.getRendition(nodeRef2, "pdf") shouldBe nodeRef2Pdf
    }

    @Test
    fun getRendition_absentRenditions() {
        alfrescoRenditionGetter.getRendition(nodeRef, "absent") shouldBe null
    }
}