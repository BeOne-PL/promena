package pl.beone.promena.alfresco.module.rendition.external

import io.kotlintest.shouldBe
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter

@RunWith(AlfrescoTestRunner::class)
class PromenaRenditionGetterTest : AbstractUtilsAlfrescoIT() {

    private lateinit var nodeRef: NodeRef
    private lateinit var nodeRefDoclib: ChildAssociationRef
    private lateinit var nodeRefDoclib2: ChildAssociationRef
    private lateinit var nodeRefPdf: ChildAssociationRef

    private lateinit var nodeRef2: NodeRef
    private lateinit var nodeRef2Pdf: ChildAssociationRef

    private lateinit var renditionGetter: RenditionGetter

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

        renditionGetter = PromenaRenditionGetter(serviceRegistry.nodeService)
    }

    @Test
    fun getRenditions() {
        renditionGetter.getRenditions(nodeRef) shouldBe
                listOf(nodeRefDoclib, nodeRefDoclib2, nodeRefPdf)

        renditionGetter.getRenditions(nodeRef2) shouldBe
                listOf(nodeRef2Pdf)
    }

    @Test
    fun getRendition() {
        renditionGetter.getRendition(nodeRef, "doclib") shouldBe nodeRefDoclib2
        renditionGetter.getRendition(nodeRef, "pdf") shouldBe nodeRefPdf

        renditionGetter.getRendition(nodeRef2, "pdf") shouldBe nodeRef2Pdf
    }

    @Test
    fun getRendition_absentRenditions() {
        renditionGetter.getRendition(nodeRef, "absent") shouldBe null
    }
}