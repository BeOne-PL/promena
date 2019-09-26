package pl.beone.promena.alfresco.module.rendition.external

import io.kotlintest.shouldThrow
import io.mockk.*
import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter
import pl.beone.promena.alfresco.module.rendition.external.DefaultAlfrescoPromenaRenditionTransformer.Companion.METADATA_RENDITION_NAME_PROPERTY
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import reactor.core.publisher.Mono
import java.time.Duration

class DefaultAlfrescoPromenaRenditionTransformerTest {

    private lateinit var sourceNodeRef: NodeRef
    private lateinit var renditionName: String
    private lateinit var childAssociationRef: ChildAssociationRef

    private lateinit var transformation: Transformation
    private lateinit var nodeDescriptors: List<NodeDescriptor>

    private lateinit var contentService: ContentService
    private lateinit var alfrescoRenditionGetter: AlfrescoRenditionGetter
    private lateinit var alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter

    @Before
    fun setUp() {
        sourceNodeRef = NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
        renditionName = "doclib"
        childAssociationRef =
            ChildAssociationRef(ASSOC_RENDITION, sourceNodeRef, null, NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"))

        val mediaType = APPLICATION_PDF
        transformation = singleTransformation("converter", mediaType, emptyParameters())
        nodeDescriptors = listOf(
            NodeDescriptor.of(sourceNodeRef, emptyMetadata() + (METADATA_RENDITION_NAME_PROPERTY to renditionName))
        )

        contentService = mockk {
            every { getReader(sourceNodeRef, PROP_CONTENT) } returns mockk {
                every { mimetype } returns mediaType.mimeType
                every { encoding } returns mediaType.charset.name()
            }
        }

        alfrescoRenditionGetter = mockk {
            every { getRendition(sourceNodeRef, renditionName) } returns childAssociationRef
        }

        alfrescoPromenaRenditionDefinitionGetter = mockk {
            every { getByRenditionName(renditionName) } returns mockk {
                every { getTransformation(mediaType) } returns transformation
            }
        }
    }

    @Test
    fun transform() {
        val timeout = Duration.ofMillis(500)

        val alfrescoPromenaRenditionInProgressSynchronizer = mockk<AlfrescoPromenaRenditionInProgressSynchronizer> {
            every { isInProgress(sourceNodeRef, renditionName) } just Runs
            every { start(sourceNodeRef, renditionName) } just Runs
            every { finish(sourceNodeRef, renditionName) } just Runs
        }

        val alfrescoPromenaTransformer = mockk<AlfrescoPromenaTransformer> {
            every { transform(transformation, nodeDescriptors, timeout) } returns emptyList()
        }

        DefaultAlfrescoPromenaRenditionTransformer(
            contentService,
            alfrescoRenditionGetter,
            alfrescoPromenaRenditionDefinitionGetter,
            alfrescoPromenaRenditionInProgressSynchronizer,
            alfrescoPromenaTransformer,
            timeout
        ).transform(sourceNodeRef, renditionName)

        verify { alfrescoRenditionGetter.getRendition(sourceNodeRef, renditionName) }

        verify { alfrescoPromenaRenditionInProgressSynchronizer.isInProgress(sourceNodeRef, renditionName) }
        verify { alfrescoPromenaRenditionInProgressSynchronizer.start(sourceNodeRef, renditionName) }
        verify { alfrescoPromenaRenditionInProgressSynchronizer.finish(sourceNodeRef, renditionName) }
    }

    @Test
    fun transformAsync() {
        val alfrescoPromenaRenditionInProgressSynchronizer = mockk<AlfrescoPromenaRenditionInProgressSynchronizer> {
            every { isInProgress(sourceNodeRef, renditionName) } just Runs
            every { start(sourceNodeRef, renditionName) } just Runs
            every { finish(sourceNodeRef, renditionName) } just Runs
        }

        val alfrescoPromenaTransformer = mockk<AlfrescoPromenaTransformer> {
            every { transformAsync(transformation, nodeDescriptors) } returns Mono.empty()
        }

        DefaultAlfrescoPromenaRenditionTransformer(
            contentService,
            alfrescoRenditionGetter,
            alfrescoPromenaRenditionDefinitionGetter,
            alfrescoPromenaRenditionInProgressSynchronizer,
            alfrescoPromenaTransformer,
            Duration.ZERO
        ).transformAsync(sourceNodeRef, renditionName)

        verify { alfrescoPromenaRenditionInProgressSynchronizer.isInProgress(sourceNodeRef, renditionName) }
        verify { alfrescoPromenaRenditionInProgressSynchronizer.start(sourceNodeRef, renditionName) }
        verify { alfrescoPromenaRenditionInProgressSynchronizer.finish(sourceNodeRef, renditionName) }
    }

    @Test
    fun `transformAsync _ synchronizer throws exception by start function _ should throw Exception and finish rendition`() {
        val exception = Exception()

        val alfrescoPromenaRenditionInProgressSynchronizer = mockk<AlfrescoPromenaRenditionInProgressSynchronizer> {
            every { isInProgress(sourceNodeRef, renditionName) } just Runs
            every { start(sourceNodeRef, renditionName) } throws exception
            every { finish(sourceNodeRef, renditionName) } just Runs
        }

        shouldThrow<Exception> {
            DefaultAlfrescoPromenaRenditionTransformer(
                contentService,
                alfrescoRenditionGetter,
                alfrescoPromenaRenditionDefinitionGetter,
                alfrescoPromenaRenditionInProgressSynchronizer,
                mockk(),
                Duration.ZERO
            ).transformAsync(sourceNodeRef, renditionName)
        }

        verify { alfrescoPromenaRenditionInProgressSynchronizer.isInProgress(sourceNodeRef, renditionName) }
        verify { alfrescoPromenaRenditionInProgressSynchronizer.start(sourceNodeRef, renditionName) }
        verify { alfrescoPromenaRenditionInProgressSynchronizer.finish(sourceNodeRef, renditionName) }
    }
}