package pl.beone.promena.alfresco.module.core.extension

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

fun Logger.start(transformation: Transformation, nodeDescriptor: NodeDescriptor) {
    info(
        "Transforming <{}> using <{}>...",
        nodeDescriptor,
        transformation
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
        "Transformed <{}> using <{}> to <{}> in <{} s>",
        nodeDescriptor,
        transformation,
        transformationExecutionResult,
        calculateExecutionTimeInSeconds(startTimestamp, endTimestamp)
    )
}

fun Logger.stoppedTransformingBecauseChecksumsAreDifferent(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    oldNodesChecksum: String,
    currentNodesChecksum: String
) {
    warn(
        "Stopped transforming <{}> using <{}> because nodes have been changed in the meantime (old checksum <{}>, current checksum <{}>)",
        nodeDescriptor,
        transformation,
        oldNodesChecksum,
        currentNodesChecksum
    )
}

fun Logger.stoppedTransformingBecauseNodeDoesNotExist(transformation: Transformation, nodeDescriptor: NodeDescriptor, nodeRef: NodeRef) {
    warn(
        "Stopped transforming <{}> using <{}> because <{}> node has been removed in the meantime",
        nodeDescriptor,
        transformation,
        nodeRef
    )
}

fun Logger.couldNotTransform(transformation: Transformation, nodeDescriptor: NodeDescriptor, exception: Throwable) {
    if (exception.cause != null) {
        error(
            "Couldn't transform <{}> using <{}>",
            nodeDescriptor,
            transformation,
            exception
        )
    } else {
        error(
            "Couldn't transform <{}> using <{}>\n{}",
            nodeDescriptor,
            transformation,
            exception.toString().addHashAtTheBeggingOfEachLine()
        )
    }
}

private fun String.addHashAtTheBeggingOfEachLine(): String =
    this.split("\n")
        .joinToString("\n") { "# $it" }

fun Logger.logOnRetry(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    attempt: Long,
    maxAttempts: Long,
    nextAttemptDelay: Duration
) {
    warn(
        "Attempt ({}/{}). Transformation <{}> using <{}> will be run after <{}>",
        attempt,
        maxAttempts,
        nodeDescriptor,
        transformation,
        nextAttemptDelay.toPrettyString()
    )
}

private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
    String.format("%.3f", (millisEnd - millisStart) / 1000.0)