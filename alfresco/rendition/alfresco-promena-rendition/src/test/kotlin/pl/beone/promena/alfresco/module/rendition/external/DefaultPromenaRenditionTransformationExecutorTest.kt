package pl.beone.promena.alfresco.module.rendition.external

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.*
import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter
import pl.beone.promena.alfresco.module.rendition.contract.definition.PromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.internal.transformation.definition.PromenaRenditionNamePromenaTransformationMetadataMappingDefinition.Companion.PROP_RENDITION_NAME_PREFIXED
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.time.Duration

class DefaultPromenaRenditionTransformationExecutorTest {

    companion object {
        private val resultNodeRef = NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "a104e65c-ca41-418e-ab4a-6e7659f60c5b")

        private val transformationExecution = transformationExecution("1")
        private val transformationExecutionResult = transformationExecutionResult(resultNodeRef)

        private val sourceNodeRef = NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
        private const val renditionName = "doclib"
        private val childAssociationRef =
            ChildAssociationRef(ASSOC_RENDITION, sourceNodeRef, null, resultNodeRef)
        private val waitMax = Duration.ofMillis(500)

        private val mediaType = APPLICATION_PDF
        private val transformation = singleTransformation("converter", mediaType, emptyParameters())
        private val nodeDescriptor = sourceNodeRef.toSingleNodeDescriptor(emptyMetadata() + (PROP_RENDITION_NAME_PREFIXED to renditionName))

    }

    private lateinit var serviceRegistry: ServiceRegistry
    private lateinit var renditionGetter: RenditionGetter
    private lateinit var promenaRenditionDefinitionGetter: PromenaRenditionDefinitionGetter
    private lateinit var promenaRenditionInProgressSynchronizer: PromenaRenditionInProgressSynchronizer
    private lateinit var promenaTransformationManager: PromenaTransformationManager

    @Before
    fun setUp() {
        serviceRegistry = mockk {
            every { contentService } returns mockk {
                every { getReader(sourceNodeRef, PROP_CONTENT) } returns mockk {
                    every { mimetype } returns mediaType.mimeType
                    every { encoding } returns mediaType.charset.name()
                }
            }
        }

        renditionGetter = mockk {
            every { getRendition(sourceNodeRef, renditionName) } returns childAssociationRef
        }

        promenaRenditionDefinitionGetter = mockk {
            every { getByRenditionName(renditionName) } returns mockk {
                every { getTransformation(mediaType) } returns transformation
            }
        }

        promenaRenditionInProgressSynchronizer = mockk<PromenaRenditionInProgressSynchronizer> {
            every { isInProgress(sourceNodeRef, renditionName) } just Runs
            every { start(sourceNodeRef, renditionName) } just Runs
            every { finish(sourceNodeRef, renditionName) } just Runs
        }

        promenaTransformationManager = mockk {
            every { getResult(transformationExecution, waitMax) } returns transformationExecutionResult
        }

    }

    @Test
    fun transform() {
        val promenaTransformationExecutor = mockk<PromenaTransformationExecutor> {
            every { execute(transformation, nodeDescriptor, any()) } returns transformationExecution
        }

        DefaultPromenaRenditionTransformationExecutor(
            serviceRegistry,
            renditionGetter,
            promenaRenditionDefinitionGetter,
            promenaRenditionInProgressSynchronizer,
            promenaTransformationExecutor,
            promenaTransformationManager,
            waitMax
        ).transform(sourceNodeRef, renditionName)

        verify { renditionGetter.getRendition(sourceNodeRef, renditionName) }

        verify { promenaRenditionInProgressSynchronizer.isInProgress(sourceNodeRef, renditionName) }
        verify { promenaRenditionInProgressSynchronizer.start(sourceNodeRef, renditionName) }
        verify(exactly = 0) { promenaRenditionInProgressSynchronizer.finish(sourceNodeRef, renditionName) }
    }

    @Test
    fun transformAsync() {
        val promenaTransformationExecutor = mockk<PromenaTransformationExecutor> {
            every { execute(transformation, nodeDescriptor, any()) } returns transformationExecution
        }

        DefaultPromenaRenditionTransformationExecutor(
            serviceRegistry,
            renditionGetter,
            promenaRenditionDefinitionGetter,
            promenaRenditionInProgressSynchronizer,
            promenaTransformationExecutor,
            promenaTransformationManager,
            waitMax
        ).transformAsync(sourceNodeRef, renditionName)

        verify(exactly = 0) { renditionGetter.getRendition(sourceNodeRef, renditionName) }

        verify { promenaRenditionInProgressSynchronizer.isInProgress(sourceNodeRef, renditionName) }
        verify { promenaRenditionInProgressSynchronizer.start(sourceNodeRef, renditionName) }
        verify(exactly = 0) { promenaRenditionInProgressSynchronizer.finish(sourceNodeRef, renditionName) }
    }

    @Test
    fun `transformAsync _ promenaTransformationExecutor throws exception _ should throw Exception and finish transformation`() {
        val exception = Exception("message")

        val promenaTransformationExecutor = mockk<PromenaTransformationExecutor> {
            every { execute(transformation, nodeDescriptor, any()) } throws exception
        }

        shouldThrowExactly<Exception> {
            DefaultPromenaRenditionTransformationExecutor(
                serviceRegistry,
                renditionGetter,
                promenaRenditionDefinitionGetter,
                promenaRenditionInProgressSynchronizer,
                promenaTransformationExecutor,
                promenaTransformationManager,
                waitMax
            ).transformAsync(sourceNodeRef, renditionName)
        }.message shouldBe "message"

        verify { promenaRenditionInProgressSynchronizer.isInProgress(sourceNodeRef, renditionName) }
        verify { promenaRenditionInProgressSynchronizer.start(sourceNodeRef, renditionName) }
        verify { promenaRenditionInProgressSynchronizer.finish(sourceNodeRef, renditionName) }
    }
}