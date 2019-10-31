package pl.beone.promena.alfresco.module.core.extension

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.extension.format
import pl.beone.promena.transformer.internal.extension.toPrettyString
import pl.beone.promena.transformer.internal.extension.toSeconds
import java.time.Duration

fun Logger.start(transformation: Transformation, nodeDescriptor: NodeDescriptor) {
    info(
        "Transforming...\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}"
    )
}

fun Logger.transformedSuccessfully(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    transformationExecutionResult: TransformationExecutionResult,
    startTimestamp: Long,
    endTimestamp: Long
) {
    info(
        "Transformed in <${(endTimestamp - startTimestamp).toSeconds().format(3)} s>\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}\n" +
                "> Result <${transformationExecutionResult.nodeRefs.size}>: ${transformationExecutionResult.toPrettyString()}"
    )
}

fun Logger.stoppedTransformingBecauseChecksumsAreDifferent(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    nodesChecksum: String,
    currentNodesChecksum: String
) {
    warn(
        "Stopped transforming because nodes have been changed in the meantime - old checksum <$nodesChecksum>, current checksum <$currentNodesChecksum>\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}"
    )
}

fun Logger.stoppedTransformingBecauseNodeDoesNotExist(transformation: Transformation, nodeDescriptor: NodeDescriptor, nodeRef: NodeRef) {
    warn(
        "Stopped transforming because node <$nodeRef> has been removed in the meantime\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}"
    )
}

fun Logger.couldNotTransform(transformation: Transformation, nodeDescriptor: NodeDescriptor, exception: Exception) {
    error(
        "Couldn't transform\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}",
        exception
    )
}

fun Logger.couldNotTransform(transformation: Transformation, nodeDescriptor: NodeDescriptor, exception: TransformationException) {
    error(
        "Couldn't transform\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}\n" +
                exception.toString().addHashAtTheBeggingOfEachLine()
    )
}

fun Logger.logOnRetry(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    attempt: Long,
    maxAttempts: Long,
    nextAttemptDelay: Duration,
    exception: TransformationException
) {
    warn(
        "Couldn't transform. Attempt ($attempt/$maxAttempts) will be made in <${nextAttemptDelay.toPrettyString()}>\n" +
                "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                "> Node descriptor <${nodeDescriptor.descriptors.size}>: ${nodeDescriptor.toPrettyString()}\n" +
                exception.toString().addHashAtTheBeggingOfEachLine()
    )
}

private fun String.addHashAtTheBeggingOfEachLine(): String =
    this.split("\n")
        .joinToString("\n") { "# $it" }
