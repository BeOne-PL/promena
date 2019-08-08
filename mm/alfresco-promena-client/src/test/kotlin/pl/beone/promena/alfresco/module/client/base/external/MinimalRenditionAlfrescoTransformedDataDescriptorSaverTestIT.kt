package pl.beone.promena.alfresco.module.client.base.external

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.shouldBe
import io.mockk.*
import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel
import pl.beone.promena.alfresco.module.client.base.applicationmodel.parameters.PromenaParametersAlfrescoConstants
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
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
class MinimalRenditionAlfrescoTransformedDataDescriptorSaverTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private val data = "test".toMemoryData()
    }

    @Test
    fun save_manyResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(data, any()) } just Runs
        }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            alfrescoDataConverter
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
                integrationNode.getAspects() shouldContain RenditionModel.ASPECT_RENDITIONED

                nodes shouldHaveSize 2
                val (node, node2) = nodes

                val transformationString = listOf(
                    "Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=application/pdf, charset=UTF-8), parameters=MapParameters(parameters={}))",
                    "Single(transformerId=TransformerId(name=transformer2, subName=sub), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={key=value}))"
                )
                val name = "transformer, transformer2|sub"

                node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        ContentModel.PROP_CREATOR to currentUserName,
                        ContentModel.PROP_MODIFIER to currentUserName,
                        ContentModel.PROP_NAME to name,
                        ContentModel.PROP_IS_INDEXED to false,
                        ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION to transformationString,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to 0,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to 2
                    )
                    properties shouldNotContainKey QName.createQName("string")
                    properties shouldNotContainKey QName.createQName("int")
                    properties shouldNotContainKey QName.createQName("long")
                    properties shouldNotContainKey QName.createQName("float")
                    properties shouldNotContainKey QName.createQName("double")
                    properties shouldNotContainKey QName.createQName("boolean")
                }

                node2.getType() shouldBe ContentModel.TYPE_THUMBNAIL
                node2.getAspects() shouldNotContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
                node2.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        ContentModel.PROP_CREATOR to currentUserName,
                        ContentModel.PROP_MODIFIER to currentUserName,
                        ContentModel.PROP_NAME to name,
                        ContentModel.PROP_IS_INDEXED to false,
                        ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION to transformationString,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to 1,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to 2,
                        QName.createQName("string") to "string",
                        QName.createQName("int") to 10,
                        QName.createQName("long") to 20L,
                        QName.createQName("float") to 30.0f,
                        QName.createQName("double") to 40.0,
                        QName.createQName("boolean") to true
                    )
                }

                nodes shouldBe
                        integrationNode.getRenditionAssociations().map { it.childRef }
                integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                        listOf(
                            QName.createQName(CONTENT_MODEL_1_0_URI, "$name - 0"),
                            QName.createQName(CONTENT_MODEL_1_0_URI, "$name - 1")
                        )

                verify(exactly = 1) { alfrescoDataConverter.saveDataInContentWriter(data, any()) }
            }
    }

    @Test
    fun save_oneResult() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            alfrescoDataConverter
        )
            .save(
                singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                listOf(integrationNode),
                singleTransformedDataDescriptor(data, emptyMetadata() + ("alf_string" to "string"))
            )
            .let { nodes ->
                integrationNode.getAspects() shouldContain RenditionModel.ASPECT_RENDITIONED

                nodes shouldHaveSize 1
                val (node) = nodes

                val name = "transformer"

                node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        ContentModel.PROP_CREATOR to currentUserName,
                        ContentModel.PROP_MODIFIER to currentUserName,
                        ContentModel.PROP_NAME to name,
                        ContentModel.PROP_IS_INDEXED to false,
                        ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION to listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to 0,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to 1,
                        QName.createQName("string") to "string"
                    )
                }

                nodes shouldBe
                        integrationNode.getRenditionAssociations().map { it.childRef }
                integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                        listOf(QName.createQName(CONTENT_MODEL_1_0_URI, name))
            }
    }

    @Test
    fun save_oneResultAndAlfrescoRenditionName() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter>()

        val alfrescoRenditionName = "pdf"
        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            alfrescoDataConverter
        )
            .save(
                singleTransformation(
                    "transformer",
                    TEXT_PLAIN,
                    emptyParameters() + (PromenaParametersAlfrescoConstants.PARAMETERS_ALFRESCO to
                            emptyParameters() + (PromenaParametersAlfrescoConstants.PARAMETERS_ALFRESCO_RENDITION_NAME to alfrescoRenditionName))
                ),
                listOf(integrationNode),
                singleTransformedDataDescriptor(noData(), emptyMetadata() + ("alf_string" to "string"))
            )
            .let { nodes ->
                integrationNode.getAspects() shouldContain RenditionModel.ASPECT_RENDITIONED

                nodes shouldHaveSize 1
                val (node) = nodes

                node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        ContentModel.PROP_CREATOR to currentUserName,
                        ContentModel.PROP_MODIFIER to currentUserName,
                        ContentModel.PROP_NAME to "transformer",
                        ContentModel.PROP_IS_INDEXED to false,
                        ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION to listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={alfresco=MapParameters(parameters={renditionName=pdf})}))"),
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to 0,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to 1,
                        QName.createQName("string") to "string"
                    )
                }

                nodes shouldBe
                        integrationNode.getRenditionAssociations().map { it.childRef }
                integrationNode.getRenditionAssociations().map { it.qName } shouldBe
                        listOf(QName.createQName(CONTENT_MODEL_1_0_URI, alfrescoRenditionName))
            }
    }

    @Test
    fun save_saveOneNodeDespiteNoResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val alfrescoDataConverter = mockk<AlfrescoDataConverter>()

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
            true,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            alfrescoDataConverter
        )
            .save(
                singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                listOf(integrationNode),
                emptyTransformedDataDescriptor()
            ).let { nodes ->
                integrationNode.getAspects() shouldContain RenditionModel.ASPECT_RENDITIONED

                nodes shouldHaveSize 1
                val (node) = nodes

                val name = "transformer"

                node.getType() shouldBe ContentModel.TYPE_THUMBNAIL
                node.getAspects() shouldNotContainAll listOf(RenditionModel.ASPECT_RENDITION2, RenditionModel.ASPECT_HIDDEN_RENDITION)
                node.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        ContentModel.PROP_CREATOR to currentUserName,
                        ContentModel.PROP_MODIFIER to currentUserName,
                        ContentModel.PROP_NAME to name,
                        ContentModel.PROP_IS_INDEXED to false,
                        ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT,
                        PromenaTransformationContentModel.PROP_TRANSFORMATION to
                                listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))")
                    )
                    properties shouldNotContainKey PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX
                    properties shouldNotContainKey PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE
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

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val nodes = MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
            false,
            serviceRegistry.nodeService,
            serviceRegistry.contentService,
            serviceRegistry.namespaceService,
            serviceRegistry.transactionService,
            alfrescoDataConverter
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