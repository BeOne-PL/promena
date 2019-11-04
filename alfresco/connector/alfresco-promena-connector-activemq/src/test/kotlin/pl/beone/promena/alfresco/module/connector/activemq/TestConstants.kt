package pl.beone.promena.alfresco.module.connector.activemq

import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import pl.beone.promena.alfresco.module.core.applicationmodel.node.plus
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.time.Duration

object TestConstants {

    val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters())
    val nodeDescriptor =
        NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c").toSingleNodeDescriptor(emptyMetadata() + ("key" to "value")) +
                NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toSingleNodeDescriptor(emptyMetadata())
    val nodeRefs = nodeDescriptor.toNodeRefs()
    val retry = customRetry(3, Duration.ofMillis(1000))
    val dataDescriptor = singleDataDescriptor("".toMemoryData(), APPLICATION_PDF, emptyMetadata())
    const val nodesChecksum = "123456789"
    const val attempt: Long = 0
    const val userName = "admin"

    val transformationExecution = transformationExecution("1")
    val transformationExecutionResult = transformationExecutionResult(NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "98c8a344-7724-473d-9dd2-c7c29b77a0ff"))
}