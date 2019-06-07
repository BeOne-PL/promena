package pl.beone.promena.alfresco.module.client.base.external

import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.client.base.applicationmodel.PromenaTransformationContentModel
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

@RunWith(AlfrescoTestRunner::class)
class RenditionAlfrescoTransformedDataDescriptorSaverTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private const val transformerId = "transformer-test"
        private val data = InMemoryData("test".toByteArray())
        private val targetMediaType = MediaTypeConstants.APPLICATION_PDF
    }

    @Test
    fun save_manyResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(eq(data), any()) } just Runs
        }

        val nodes = RenditionAlfrescoTransformedDataDescriptorSaver(true,
                                                                    serviceRegistry.nodeService,
                                                                    serviceRegistry.contentService,
                                                                    serviceRegistry.namespaceService,
                                                                    serviceRegistry.transactionService,
                                                                    alfrescoDataConverter)
                .save(transformerId,
                      listOf(integrationNode),
                      targetMediaType,
                      listOf(TransformedDataDescriptor(data, MapMetadata.empty()),
                             TransformedDataDescriptor(data, MapMetadata(mapOf("alf_string" to "string",
                                                                               "alf_int" to 10,
                                                                               "alf_long" to 20L,
                                                                               "alf_float" to 30.0f,
                                                                               "alf_double" to 40.0,
                                                                               "alf_boolean" to true)))))

        nodes shouldHaveSize 2
        val (node, node2) = nodes

        node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
        node.getAspects() shouldContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
        node.getProperties().let {
            it shouldContainAll mapOf(ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                                      ContentModel.PROP_NAME to transformerId,
                                      ContentModel.PROP_THUMBNAIL_NAME to transformerId,
                                      ContentModel.PROP_IS_INDEXED to false,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_INDEX to 0,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_SIZE to 2)
            it shouldNotContainKey QName.createQName("string")
            it shouldNotContainKey QName.createQName("int")
            it shouldNotContainKey QName.createQName("long")
            it shouldNotContainKey QName.createQName("float")
            it shouldNotContainKey QName.createQName("double")
            it shouldNotContainKey QName.createQName("boolean")
        }

        node2.getType() shouldBe ContentModel.TYPE_THUMBNAIL
        node2.getAspects() shouldContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
        node2.getProperties().let {
            it shouldContainAll mapOf(ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                                      ContentModel.PROP_NAME to transformerId,
                                      ContentModel.PROP_THUMBNAIL_NAME to transformerId,
                                      ContentModel.PROP_IS_INDEXED to false,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_INDEX to 1,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_SIZE to 2,
                                      QName.createQName("string") to "string",
                                      QName.createQName("int") to 10,
                                      QName.createQName("long") to 20L,
                                      QName.createQName("float") to 30.0f,
                                      QName.createQName("double") to 40.0,
                                      QName.createQName("boolean") to true)
        }

        nodes shouldBe integrationNode.getRenditionAssociations().map { it.childRef }
        integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                listOf(QName.createQName(CONTENT_MODEL_1_0_URI, "transformer-test-1"),
                       QName.createQName(CONTENT_MODEL_1_0_URI, "transformer-test-2"))
    }

    @Test
    fun save_oneResult() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val nodes = RenditionAlfrescoTransformedDataDescriptorSaver(true,
                                                                    serviceRegistry.nodeService,
                                                                    serviceRegistry.contentService,
                                                                    serviceRegistry.namespaceService,
                                                                    serviceRegistry.transactionService,
                                                                    alfrescoDataConverter)
                .save(transformerId,
                      listOf(integrationNode),
                      targetMediaType,
                      listOf(TransformedDataDescriptor(data, MapMetadata(mapOf("alf_string" to "string")))))

        nodes shouldHaveSize 1
        val (node) = nodes

        node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
        node.getAspects() shouldContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
        node.getProperties().let {
            it shouldContainAll mapOf(ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                                      ContentModel.PROP_NAME to transformerId,
                                      ContentModel.PROP_THUMBNAIL_NAME to transformerId,
                                      ContentModel.PROP_IS_INDEXED to false,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_INDEX to 0,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_SIZE to 1,
                                      QName.createQName("string") to "string")
        }

        nodes shouldBe integrationNode.getRenditionAssociations().map { it.childRef }
        integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                listOf(QName.createQName(CONTENT_MODEL_1_0_URI, "transformer-test"))
    }

    @Test
    fun save_saveOneNodeDespiteNoResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val nodes = RenditionAlfrescoTransformedDataDescriptorSaver(true,
                                                                    serviceRegistry.nodeService,
                                                                    serviceRegistry.contentService,
                                                                    serviceRegistry.namespaceService,
                                                                    serviceRegistry.transactionService,
                                                                    alfrescoDataConverter)
                .save(transformerId, listOf(integrationNode), targetMediaType, listOf())

        nodes shouldHaveSize 1
        val (node) = nodes

        node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
        node.getAspects() shouldContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
        node.getProperties().let {
            it shouldContainAll mapOf(ContentModel.PROP_NAME to transformerId,
                                      ContentModel.PROP_THUMBNAIL_NAME to transformerId,
                                      ContentModel.PROP_IS_INDEXED to false,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_INDEX to null,
                                      PromenaTransformationContentModel.PROP_TRANSFORMATION_SIZE to 0)
            it shouldNotContainKey ContentModel.PROP_CONTENT_PROPERTY_NAME
        }

        nodes shouldBe integrationNode.getRenditionAssociations().map { it.childRef }
        integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                listOf(QName.createQName(CONTENT_MODEL_1_0_URI, "transformer-test"))
    }

    @Test
    fun save_saveNothing() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val nodes = RenditionAlfrescoTransformedDataDescriptorSaver(false,
                                                                    serviceRegistry.nodeService,
                                                                    serviceRegistry.contentService,
                                                                    serviceRegistry.namespaceService,
                                                                    serviceRegistry.transactionService,
                                                                    alfrescoDataConverter)
                .save(transformerId, listOf(integrationNode), targetMediaType, emptyList())

        nodes shouldHaveSize 0
        nodes shouldBe integrationNode.getRenditionAssociations()
    }

    private fun createNodeInIntegrationFolder(): NodeRef =
            with(createOrGetIntegrationTestsFolder()) {
                createNode()
            }
}