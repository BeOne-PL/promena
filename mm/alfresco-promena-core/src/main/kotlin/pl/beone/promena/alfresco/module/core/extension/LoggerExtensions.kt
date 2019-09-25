package pl.beone.promena.alfresco.module.core.extension

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

fun Logger.startSync(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, waitMax: Duration?) {
    info(
        "Transforming <{}> using <{}>. Waiting <{}> for response...",
        nodeDescriptors,
        transformation,
        waitMax.toPrettyString()
    )
}

fun Logger.startAsync(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>) {
    info(
        "Transforming <{}> using <{}>...",
        nodeDescriptors,
        transformation
    )
}

fun Logger.transformedSuccessfully(
    transformation: Transformation,
    nodeDescriptors: List<NodeDescriptor>,
    targetNodeRefs: List<NodeRef>,
    startTimestamp: Long,
    endTimestamp: Long
) {
    info(
        "Transformed <{}> using <{}> to <{}> in <{} s>",
        nodeDescriptors,
        transformation,
        targetNodeRefs,
        calculateExecutionTimeInSeconds(startTimestamp, endTimestamp)
    )
}

fun Logger.skippedSavingResult(
    transformation: Transformation,
    nodeDescriptors: List<NodeDescriptor>,
    oldNodesChecksum: String,
    currentNodesChecksum: String
) {
    warn(
        "Skipped saving result <{}> from <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
        nodeDescriptors,
        transformation,
        oldNodesChecksum,
        currentNodesChecksum
    )
}

fun Logger.couldNotTransformButChecksumsAreDifferent(
    transformation: Transformation,
    nodeDescriptors: List<NodeDescriptor>,
    oldNodesChecksum: String,
    currentNodesChecksum: String,
    exception: Throwable
) {
    if (exception.cause != null) {
        warn(
            "Couldn't transform <{}> using <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
            nodeDescriptors,
            transformation,
            oldNodesChecksum,
            currentNodesChecksum,
            exception
        )
    } else {
        warn(
            "Couldn't transform <{}> using <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...\n> {}",
            nodeDescriptors,
            transformation,
            oldNodesChecksum,
            currentNodesChecksum,
            exception.toString()
        )
    }
}

fun Logger.couldNotTransform(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, exception: Throwable) {
    if (exception.cause != null) {
        error(
            "Couldn't transform <{}> using <{}>",
            nodeDescriptors,
            transformation,
            exception
        )
    } else {
        error(
            "Couldn't transform <{}> using <{}>\n{}",
            nodeDescriptors,
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
    nodeDescriptors: List<NodeDescriptor>,
    attempt: Long,
    maxAttempts: Long,
    nextAttemptDelay: Duration
) {
    warn(
        "Attempt ({}/{}). Transformation <{}> using <{}> will be run after <{}>",
        attempt,
        maxAttempts,
        nodeDescriptors,
        transformation,
        nextAttemptDelay.toPrettyString()
    )
}

private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
    String.format("%.3f", (millisEnd - millisStart) / 1000.0)