package pl.beone.promena.alfresco.module.core.extension

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

fun Logger.skippedSavingResult(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    oldNodesChecksum: String,
    currentNodesChecksum: String
) {
    warn(
        "Skipped saving result <{}> from <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
        nodeDescriptor,
        transformation,
        oldNodesChecksum,
        currentNodesChecksum
    )
}

fun Logger.couldNotTransformButChecksumsAreDifferent(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    oldNodesChecksum: String,
    currentNodesChecksum: String,
    exception: Throwable
) {
    if (exception.cause != null) {
        warn(
            "Couldn't transform <{}> using <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
            nodeDescriptor,
            transformation,
            oldNodesChecksum,
            currentNodesChecksum,
            exception
        )
    } else {
        warn(
            "Couldn't transform <{}> using <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...\n> {}",
            nodeDescriptor,
            transformation,
            oldNodesChecksum,
            currentNodesChecksum,
            exception.toString()
        )
    }
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