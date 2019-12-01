package pl.beone.promena.alfresco.module.core.external.node

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
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_ID
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_RENDITION_NAME
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION_DATA_INDEX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION_DATA_SIZE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION_ID
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataSaver
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.emptyTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.data.noData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus

@RunWith(AlfrescoTestRunner::class)
class MinimalRenditionTransformedDataDescriptorSaverTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private val data = "test".toMemoryData()
    }

    private val promenaTransformationMetadataSaver = object : PromenaTransformationMetadataSaver {
        override fun save(
            sourceNodeRef: NodeRef,
            transformation: Transformation,
            transformedDataDescriptor: TransformedDataDescriptor,
            transformedNodeRefs: List<NodeRef>
        ) {
            try {
                val latitude = transformedDataDescriptor.descriptors
                    .mapNotNull {it.metadata.getOrNull(PROP_LATITUDE.localName, Double::class.java)}
                    .firstOrNull() ?: throw NoSuchElementException()
                serviceRegistry.nodeService.setProperty(transformedNodeRefs[0], PROP_LATITUDE, latitude)
            } catch (e: NoSuchElementException) {
            }
        }
    }

    @Test
    fun save_manyResults() {
        val integrationNode = createNodeInIntegrationFolder()

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(data, any()) } just Runs
        }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        MinimalRenditionTransformedDataDescriptorSaver(true, listOf(promenaTransformationMetadataSaver), dataConverter, serviceRegistry)
            .save(
                singleTransformation("transformer", APPLICATION_PDF, emptyParameters()) next
                        singleTransformation("transformer2", "sub", TEXT_PLAIN, emptyParameters() + ("key" to "value")),
                listOf(integrationNode),
                singleTransformedDataDescriptor(data, emptyMetadata() + (PROP_LATITUDE.localName to 5.5)) +
                        singleTransformedDataDescriptor(noData(), emptyMetadata())
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
                        PROPERTY_TRANSFORMATION to transformationString,
                        PROPERTY_TRANSFORMATION_ID to transformationIdString,
                        PROPERTY_TRANSFORMATION_DATA_INDEX to 0,
                        PROPERTY_TRANSFORMATION_DATA_SIZE to 2,
                        PROP_LATITUDE to 5.5
                    )
                    properties shouldContainKey PROPERTY_ID
                    properties shouldNotContainKey PROPERTY_RENDITION_NAME
                }

                node2.getType() shouldBe TYPE_THUMBNAIL
                node2.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
                node2.getProperties().let { properties ->
                    properties shouldContainAll mapOf(
                        PROP_CREATOR to currentUserName,
                        PROP_MODIFIER to currentUserName,
                        PROP_NAME to name,
                        PROP_IS_INDEXED to false,
                        PROPERTY_TRANSFORMATION to transformationString,
                        PROPERTY_TRANSFORMATION_ID to transformationIdString,
                        PROPERTY_TRANSFORMATION_DATA_INDEX to 1,
                        PROPERTY_TRANSFORMATION_DATA_SIZE to 2
                    )
                    properties shouldContainKey PROPERTY_ID
                    properties shouldNotContainKey PROPERTY_RENDITION_NAME
                    properties shouldNotContainKey PROP_LATITUDE
                }

                node.getProperty(PROPERTY_ID) shouldBe node2.getProperty(PROPERTY_ID)

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

        MinimalRenditionTransformedDataDescriptorSaver(true, emptyList(), dataConverter, serviceRegistry)
            .save(
                singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                listOf(integrationNode),
                singleTransformedDataDescriptor(data, emptyMetadata())
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
                        PROPERTY_TRANSFORMATION to listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                        PROPERTY_TRANSFORMATION_ID to listOf("transformer"),
                        PROPERTY_TRANSFORMATION_DATA_INDEX to 0,
                        PROPERTY_TRANSFORMATION_DATA_SIZE to 1
                    )
                    properties shouldContainKey PROPERTY_ID
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

        MinimalRenditionTransformedDataDescriptorSaver(true, emptyList(), dataConverter, serviceRegistry)
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
                        PROPERTY_TRANSFORMATION to
                                listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                        PROPERTY_TRANSFORMATION_ID to listOf("transformer")
                    )
                    properties shouldContainKey PROPERTY_ID
                    properties shouldNotContainKey PROPERTY_RENDITION_NAME
                    properties shouldNotContainKey PROPERTY_TRANSFORMATION_DATA_INDEX
                    properties shouldNotContainKey PROPERTY_TRANSFORMATION_DATA_SIZE
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

        MinimalRenditionTransformedDataDescriptorSaver(false, emptyList(), dataConverter, serviceRegistry)
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