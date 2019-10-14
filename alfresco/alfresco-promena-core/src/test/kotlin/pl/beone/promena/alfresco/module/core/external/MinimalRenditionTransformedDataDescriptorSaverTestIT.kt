package pl.beone.promena.alfresco.module.core.external

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.shouldBe
import io.mockk.*
import org.alfresco.model.ContentModel.*
import org.alfresco.model.RenditionModel.*
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_ID
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION_DATA_INDEX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION_DATA_SIZE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION_ID
import pl.beone.promena.alfresco.module.core.contract.DataConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.emptyTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.noData
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus

@RunWith(AlfrescoTestRunner::class)
class MinimalRenditionTransformedDataDescriptorSaverTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private val data = "test".toMemoryData()
    }

    @Test
    fun save_manyResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(data, any()) } just Runs
        }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            dataConverter
        )
            .save(
                singleTransformation("transformer", APPLICATION_PDF, emptyParameters()) next
                        singleTransformation("transformer2", "sub", TEXT_PLAIN, emptyParameters() + ("key" to "value")),
                listOf(integrationNode),
                singleTransformedDataDescriptor(data, emptyMetadata()) +
                        singleTransformedDataDescriptor(
                            noData(),
                            emptyMetadata() +
                                    ("alf_string" to "string") +
                                    ("alf_int" to 10) +
                                    ("alf_long" to 20L) +
                                    ("alf_float" to 30.0f) +
                                    ("alf_double" to 40.0) +
                                    ("alf_boolean" to true)
                        )
            )
            .let { nodes ->
                integrationNode.getAspects() shouldContain ASPECT_RENDITIONED

                nodes shouldHaveSize 2
                val (node, node2) = nodes

                val transformationString = listOf(
                    "Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=application/pdf, charset=UTF-8), parameters=MapParameters(parameters={}))",
                    "Single(transformerId=TransformerId(name=transformer2, subName=sub), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={key=value}))"
                )
                val transformationIdString = listOf(
                    "transformer",
                    "transformer2-sub"
                )
                val name = "transformer, transformer2-sub"

                node.getType() shouldBe TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        PROP_CREATOR to currentUserName,
                        PROP_MODIFIER to currentUserName,
                        PROP_NAME to name,
                        PROP_IS_INDEXED to false,
                        PROP_TRANSFORMATION to transformationString,
                        PROP_TRANSFORMATION_ID to transformationIdString,
                        PROP_TRANSFORMATION_DATA_INDEX to 0,
                        PROP_TRANSFORMATION_DATA_SIZE to 2
                    )
                    properties shouldContainKey PROP_ID
                    properties shouldNotContainKey PROP_RENDITION_NAME
                    properties shouldNotContainKey QName.createQName("string")
                    properties shouldNotContainKey QName.createQName("int")
                    properties shouldNotContainKey QName.createQName("long")
                    properties shouldNotContainKey QName.createQName("float")
                    properties shouldNotContainKey QName.createQName("double")
                    properties shouldNotContainKey QName.createQName("boolean")
                }

                node2.getType() shouldBe TYPE_THUMBNAIL
                node2.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
                node2.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        PROP_CREATOR to currentUserName,
                        PROP_MODIFIER to currentUserName,
                        PROP_NAME to name,
                        PROP_IS_INDEXED to false,
                        PROP_TRANSFORMATION to transformationString,
                        PROP_TRANSFORMATION_ID to transformationIdString,
                        PROP_TRANSFORMATION_DATA_INDEX to 1,
                        PROP_TRANSFORMATION_DATA_SIZE to 2,
                        QName.createQName("string") to "string",
                        QName.createQName("int") to 10,
                        QName.createQName("long") to 20L,
                        QName.createQName("float") to 30.0f,
                        QName.createQName("double") to 40.0,
                        QName.createQName("boolean") to true
                    )
                    properties shouldContainKey PROP_ID
                    properties shouldNotContainKey PROP_RENDITION_NAME
                }

                node.getProperty(PROP_ID) shouldBe node2.getProperty(PROP_ID)

                nodes shouldBe
                        integrationNode.getRenditionAssociations().map { it.childRef }
                integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                        listOf(
                            QName.createQName(CONTENT_MODEL_1_0_URI, name),
                            QName.createQName(CONTENT_MODEL_1_0_URI, name)
                        )

                verify(exactly = 1) { dataConverter.saveDataInContentWriter(data, any()) }
            }
    }

    @Test
    fun save_oneResult() {
        val integrationNode = createNodeInIntegrationFolder()

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            dataConverter
        )
            .save(
                singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                listOf(integrationNode),
                singleTransformedDataDescriptor(
                    data,
                    emptyMetadata() +
                            ("alf_promena:renditionName" to "doclib") +
                            ("alf_string" to "string")
                )
            )
            .let { nodes ->
                integrationNode.getAspects() shouldContain ASPECT_RENDITIONED

                nodes shouldHaveSize 1
                val (node) = nodes

                val name = "transformer"

                node.getType() shouldBe TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        PROP_CREATOR to currentUserName,
                        PROP_MODIFIER to currentUserName,
                        PROP_NAME to name,
                        PROP_IS_INDEXED to false,
                        PROP_TRANSFORMATION to listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                        PROP_TRANSFORMATION_ID to listOf("transformer"),
                        PROP_TRANSFORMATION_DATA_INDEX to 0,
                        PROP_TRANSFORMATION_DATA_SIZE to 1,
                        PROP_RENDITION_NAME to "doclib",
                        QName.createQName("string") to "string"
                    )
                    properties shouldContainKey PROP_ID
                }

                nodes shouldBe
                        integrationNode.getRenditionAssociations().map { it.childRef }
                integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                        listOf(QName.createQName(CONTENT_MODEL_1_0_URI, name))
            }
    }

    @Test
    fun save_saveOneNodeDespiteNoResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val dataConverter = mockk<DataConverter>()

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            dataConverter
        )
            .save(
                singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                listOf(integrationNode),
                emptyTransformedDataDescriptor()
            ).let { nodes ->
                integrationNode.getAspects() shouldContain ASPECT_RENDITIONED

                nodes shouldHaveSize 1
                val (node) = nodes

                val name = "transformer"

                node.getType() shouldBe TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        PROP_CREATOR to currentUserName,
                        PROP_MODIFIER to currentUserName,
                        PROP_NAME to name,
                        PROP_IS_INDEXED to false,
                        PROP_TRANSFORMATION to
                                listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                        PROP_TRANSFORMATION_ID to listOf("transformer")
                    )
                    properties shouldContainKey PROP_ID
                    properties shouldNotContainKey PROP_RENDITION_NAME
                    properties shouldNotContainKey PROP_TRANSFORMATION_DATA_INDEX
                    properties shouldNotContainKey PROP_TRANSFORMATION_DATA_SIZE
                }

                nodes shouldBe
                        integrationNode.getRenditionAssociations().map { it.childRef }
                integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                        listOf(QName.createQName(CONTENT_MODEL_1_0_URI, name))
            }
    }

    @Test
    fun save_saveNothing() {
        val integrationNode = createNodeInIntegrationFolder()

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val nodes = MinimalRenditionTransformedDataDescriptorSaver(
            false,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            dataConverter
        )
            .save(
                singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                listOf(integrationNode),
                emptyTransformedDataDescriptor()
            )
            .let { nodes ->
                nodes shouldHaveSize 0
                nodes shouldBe
                        integrationNode.getRenditionAssociations()
            }
    }

    private fun createNodeInIntegrationFolder(): NodeRef =
        with(createOrGetIntegrationTestsFolder()) {
            createNode()
        }
}