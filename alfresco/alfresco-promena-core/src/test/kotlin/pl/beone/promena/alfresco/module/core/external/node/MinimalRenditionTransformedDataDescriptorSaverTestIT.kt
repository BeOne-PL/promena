@file:Suppress("UNCHECKED_CAST")

package pl.beone.promena.alfresco.module.core.external.node

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.matchers.collections.shouldNotContainAll
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.shouldBe
import io.mockk.*
import org.alfresco.model.ContentModel.*
import org.alfresco.model.RenditionModel.*
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName.createQName
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_EXECUTION_ID
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_EXECUTION_IDS
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
import java.io.Serializable

@Suppress("DEPRECATION")
@RunWith(AlfrescoTestRunner::class)
class MinimalRenditionTransformedDataDescriptorSaverTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private val data = "test".toMemoryData()

        private const val executionId = "e5150ff7-0914-4851-941f-0c3b0a4af317"
    }

    private val promenaTransformationMetadataSaver = object : PromenaTransformationMetadataSaver {
        override fun save(
            nodeRefs: List<NodeRef>,
            transformation: Transformation,
            transformedDataDescriptor: TransformedDataDescriptor,
            transformedNodeRefs: List<NodeRef>
        ) {
            try {
                val latitude = transformedDataDescriptor.descriptors
                    .mapNotNull { it.metadata.getOrNull(PROP_LATITUDE.localName, Double::class.java) }
                    .firstOrNull() ?: throw NoSuchElementException()

                (nodeRefs + transformedNodeRefs).forEach { serviceRegistry.nodeService.setProperty(it, PROP_LATITUDE, latitude) }

            } catch (e: NoSuchElementException) {
            }
        }
    }

    @Test
    fun save_manyResults() {
        val integrationFolder = createNodeInIntegrationFolder()
        val nodeRef = integrationFolder.createNode()
        val nodeRef2 = integrationFolder.createNode()
        val nodeRefs = listOf(nodeRef, nodeRef2)
        val latitudeValue = 5.5

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(data, any()) } just Runs
        }

        val additionalExecutionId = "7abdf1e2-92f4-47b2-983a-611e42f3555c"
        nodeRefs.forEach { serviceRegistry.nodeService.setProperty(it, PROPERTY_EXECUTION_IDS, listOf(additionalExecutionId) as Serializable) }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        with(
            MinimalRenditionTransformedDataDescriptorSaver(true, listOf(promenaTransformationMetadataSaver), dataConverter, serviceRegistry)
                .save(
                    executionId,
                    singleTransformation("transformer", APPLICATION_PDF, emptyParameters()) next
                            singleTransformation("transformer2", "sub", TEXT_PLAIN, emptyParameters() + ("key" to "value")),
                    nodeRefs,
                    singleTransformedDataDescriptor(data, emptyMetadata() + (PROP_LATITUDE.localName to latitudeValue)) +
                            singleTransformedDataDescriptor(noData(), emptyMetadata())
                )
        ) {
            nodeRef.getAspects() shouldContain ASPECT_RENDITIONED
            nodeRef.getProperty(PROP_LATITUDE) shouldBe latitudeValue
            nodeRef2.getAspects() shouldContain ASPECT_RENDITIONED
            nodeRef2.getProperty(PROP_LATITUDE) shouldBe latitudeValue
            nodeRefs.forEach { it.getProperty(PROPERTY_EXECUTION_IDS) shouldBe listOf(additionalExecutionId, executionId) }

            this shouldHaveSize 2
            val (transformedNodeRef, transformedNodeRef2) = this

            val transformationString = listOf(
                "Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=application/pdf, charset=UTF-8), parameters=MapParameters(parameters={}))",
                "Single(transformerId=TransformerId(name=transformer2, subName=sub), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={key=value}))"
            )
            val transformationIdString = listOf("transformer", "transformer2-sub")
            val name = "transformer, transformer2-sub"

            transformedNodeRef.getType() shouldBe TYPE_THUMBNAIL
            transformedNodeRef.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
            with(transformedNodeRef.getProperties()) {
                this shouldContainAll mapOf(
                    PROP_CREATOR to currentUserName,
                    PROP_MODIFIER to currentUserName,
                    PROP_NAME to name,
                    PROPERTY_EXECUTION_ID to executionId,
                    PROPERTY_TRANSFORMATION to transformationString,
                    PROPERTY_TRANSFORMATION_ID to transformationIdString,
                    PROPERTY_TRANSFORMATION_DATA_INDEX to 0,
                    PROPERTY_TRANSFORMATION_DATA_SIZE to 2,
                    PROP_LATITUDE to latitudeValue
                )
                this shouldNotContainKey PROPERTY_RENDITION_NAME
            }

            transformedNodeRef2.getType() shouldBe TYPE_THUMBNAIL
            transformedNodeRef2.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
            with(transformedNodeRef2.getProperties()) {
                this shouldContainAll mapOf(
                    PROP_CREATOR to currentUserName,
                    PROP_MODIFIER to currentUserName,
                    PROP_NAME to name,
                    PROPERTY_EXECUTION_ID to executionId,
                    PROPERTY_TRANSFORMATION to transformationString,
                    PROPERTY_TRANSFORMATION_ID to transformationIdString,
                    PROPERTY_TRANSFORMATION_DATA_INDEX to 1,
                    PROPERTY_TRANSFORMATION_DATA_SIZE to 2,
                    PROP_LATITUDE to latitudeValue
                )
                this shouldNotContainKey PROPERTY_RENDITION_NAME
            }

            this shouldBe nodeRef.getRenditionAssociations().map { it.childRef }
            transformedNodeRef.getProperty(PROPERTY_EXECUTION_ID) shouldBe transformedNodeRef2.getProperty(PROPERTY_EXECUTION_ID)
            transformedNodeRef.getProperty(PROPERTY_EXECUTION_ID) shouldBe executionId
            transformedNodeRef2.getProperty(PROPERTY_EXECUTION_ID) shouldBe executionId

            val renditionNames = List(2) { createQName(CONTENT_MODEL_1_0_URI, name) }
            nodeRef.getRenditionAssociations().map { it.qName } shouldBe renditionNames
            nodeRef2.getRenditionAssociations().map { it.qName } shouldBe renditionNames

            verify(exactly = 1) { dataConverter.saveDataInContentWriter(data, any()) }
        }
    }

    @Test
    fun save_oneResult() {
        val integrationFolder = createNodeInIntegrationFolder()
        val nodeRef = integrationFolder.createNode()
        val nodeRef2 = integrationFolder.createNode()
        val nodeRefs = listOf(nodeRef, nodeRef2)

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        with(
            MinimalRenditionTransformedDataDescriptorSaver(true, emptyList(), dataConverter, serviceRegistry)
                .save(
                    executionId,
                    singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                    nodeRefs,
                    singleTransformedDataDescriptor(data, emptyMetadata())
                )
        ) {
            nodeRef.getAspects() shouldContain ASPECT_RENDITIONED
            nodeRef2.getAspects() shouldContain ASPECT_RENDITIONED
            nodeRefs.forEach { it.getProperty(PROPERTY_EXECUTION_IDS) shouldBe listOf(executionId) }

            this shouldHaveSize 1
            val transformedNodeRef = this[0]

            val name = "transformer"

            transformedNodeRef.getType() shouldBe TYPE_THUMBNAIL
            transformedNodeRef.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
            transformedNodeRef.getProperties() shouldContainAll mapOf(
                PROP_CREATOR to currentUserName,
                PROP_MODIFIER to currentUserName,
                PROP_NAME to name,
                PROPERTY_EXECUTION_ID to executionId,
                PROPERTY_TRANSFORMATION to listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                PROPERTY_TRANSFORMATION_ID to listOf("transformer"),
                PROPERTY_TRANSFORMATION_DATA_INDEX to 0,
                PROPERTY_TRANSFORMATION_DATA_SIZE to 1
            )

            this shouldBe nodeRef.getRenditionAssociations().map { it.childRef }
            transformedNodeRef.getProperty(PROPERTY_EXECUTION_ID) shouldBe executionId

            val renditionNames = listOf(createQName(CONTENT_MODEL_1_0_URI, name))
            nodeRef.getRenditionAssociations().map { it.qName } shouldBe renditionNames
            nodeRef2.getRenditionAssociations().map { it.qName } shouldBe renditionNames

            verify(exactly = 1) { dataConverter.saveDataInContentWriter(data, any()) }
        }
    }

    @Test
    fun save_saveOneNodeDespiteNoResults() {
        val integrationFolder = createNodeInIntegrationFolder()
        val nodeRef = integrationFolder.createNode()
        val nodeRef2 = integrationFolder.createNode()
        val nodeRefs = listOf(nodeRef, nodeRef2)

        val dataConverter = mockk<DataConverter>()

        val currentUserName = serviceRegistry.authenticationService.currentUserName

        with(
            MinimalRenditionTransformedDataDescriptorSaver(true, emptyList(), dataConverter, serviceRegistry)
                .save(
                    executionId,
                    singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                    nodeRefs,
                    emptyTransformedDataDescriptor()
                )
        ) {
            nodeRef.getAspects() shouldContain ASPECT_RENDITIONED
            nodeRef2.getAspects() shouldContain ASPECT_RENDITIONED

            this shouldHaveSize 1
            val transformedNodeRef = this[0]

            val name = "transformer"

            transformedNodeRef.getType() shouldBe TYPE_THUMBNAIL
            transformedNodeRef.getAspects() shouldNotContainAll listOf(ASPECT_RENDITION2, ASPECT_HIDDEN_RENDITION)
            with(transformedNodeRef.getProperties()) {
                this shouldContainAll mapOf(
                    PROP_CREATOR to currentUserName,
                    PROP_MODIFIER to currentUserName,
                    PROP_NAME to name,
                    PROPERTY_EXECUTION_ID to executionId,
                    PROPERTY_TRANSFORMATION to
                            listOf("Single(transformerId=TransformerId(name=transformer, subName=null), targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))"),
                    PROPERTY_TRANSFORMATION_ID to listOf("transformer")
                )
                this shouldNotContainKey PROPERTY_RENDITION_NAME
                this shouldNotContainKey PROPERTY_TRANSFORMATION_DATA_INDEX
                this shouldNotContainKey PROPERTY_TRANSFORMATION_DATA_SIZE
            }

            this shouldBe nodeRef.getRenditionAssociations().map { it.childRef }
            transformedNodeRef.getProperty(PROPERTY_EXECUTION_ID) shouldBe executionId

            val renditionNames = listOf(createQName(CONTENT_MODEL_1_0_URI, name))
            nodeRef.getRenditionAssociations().map { it.qName } shouldBe renditionNames
            nodeRef2.getRenditionAssociations().map { it.qName } shouldBe renditionNames

            verify(exactly = 0) { dataConverter.saveDataInContentWriter(data, any()) }
        }
    }

    @Test
    fun save_saveNothing() {
        val integrationFolder = createNodeInIntegrationFolder()
        val nodeRef = integrationFolder.createNode()
        val nodeRef2 = integrationFolder.createNode()
        val nodeRefs = listOf(nodeRef, nodeRef2)

        val dataConverter = mockk<DataConverter> {
            every { saveDataInContentWriter(any(), any()) } just Runs
        }

        with(
            MinimalRenditionTransformedDataDescriptorSaver(false, emptyList(), dataConverter, serviceRegistry)
                .save(
                    executionId,
                    singleTransformation("transformer", TEXT_PLAIN, emptyParameters()),
                    nodeRefs,
                    emptyTransformedDataDescriptor()
                )
        ) {
            nodeRefs.forEach { it.getAspects() shouldNotContain ASPECT_RENDITIONED }
            nodeRefs.forEach { it.getProperty(PROPERTY_EXECUTION_IDS) shouldBe listOf(executionId) }

            this shouldHaveSize 0
        }
    }

    private fun createNodeInIntegrationFolder(): NodeRef =
        createOrGetIntegrationTestsFolder()
            .also { it.createNode() }
}