package pl.beone.promena.alfresco.module.connector.http

import io.mockk.mockk
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import pl.beone.promena.alfresco.module.core.applicationmodel.node.plus
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.time.Duration

object TestConstants {

    const val threads = 1

    val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters())
    val nodeDescriptor =
        NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c").toSingleNodeDescriptor(emptyMetadata() + ("key" to "value")) +
                NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toSingleNodeDescriptor(emptyMetadata())
    val postTransformationExecutor = mockk<PostTransformationExecutor>()

    val nodeRefs = nodeDescriptor.toNodeRefs()
    val retry = customRetry(3, Duration.ofSeconds(2))
    val dataDescriptor = singleDataDescriptor("".toMemoryData(), APPLICATION_PDF, emptyMetadata())
    const val nodesChecksum = "123456789"
    const val userName = "admin"

    val externalCommunicationParameters = memoryCommunicationParameters()
    val transformationDescriptor = transformationDescriptor(transformation, dataDescriptor, externalCommunicationParameters)
    val transformationDescriptorBytes = "serialized".toByteArray()

    val transformationExecutionResult = transformationExecutionResult(NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "98c8a344-7724-473d-9dd2-c7c29b77a0ff"))

    val performedTransformationDescriptor = performedTransformationDescriptor(
        singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value"))
    )
    val performedTransformationDescriptorBytes = "to deserialize".toByteArray()
}