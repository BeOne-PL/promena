package pl.beone.promena.alfresco.module.client.base.extension

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

fun Logger.startSync(transformation: Transformation, nodeRefs: List<NodeRef>, waitMax: Duration?) {
    info(
        "Transforming <{}> nodes <{}>. Waiting <{}> for response...",
        transformation,
        nodeRefs,
        waitMax.toPrettyString()
    )
}

fun Logger.startAsync(transformation: Transformation, nodeRefs: List<NodeRef>) {
    info(
        "Transforming <{}> nodes <{}>...",
        transformation,
        nodeRefs
    )
}

fun Logger.transformedSuccessfully(
    transformation: Transformation,
    nodeRefs: List<NodeRef>,
    targetNodeRefs: List<NodeRef>,
    startTimestamp: Long,
    endTimestamp: Long
) {
    info(
        "Transformed <{}> nodes <{}> to <{}> in <{} s>",
        transformation,
        nodeRefs,
        targetNodeRefs,
        calculateExecutionTimeInSeconds(startTimestamp, endTimestamp)
    )
}

fun Logger.skippedSavingResult(transformation: Transformation, nodeRefs: List<NodeRef>, oldNodesChecksum: String, currentNodesChecksum: String) {
    warn(
        "Skipped saving result <{}> nodes <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
        transformation,
        nodeRefs,
        oldNodesChecksum,
        currentNodesChecksum
    )
}

fun Logger.couldNotTransformButChecksumsAreDifferent(
    transformation: Transformation,
    nodeRefs: List<NodeRef>,
    oldNodesChecksum: String,
    currentNodesChecksum: String,
    exception: Throwable
) {
    if (exception.cause != null) {
        warn(
            "Couldn't transform <{}> nodes <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
            transformation,
            nodeRefs,
            oldNodesChecksum,
            currentNodesChecksum,
            exception
        )
    } else {
        warn(
            "Couldn't transform <{}> nodes <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...\n> {}",
            transformation,
            nodeRefs,
            oldNodesChecksum,
            currentNodesChecksum,
            exception.toString()
        )
    }
}

fun Logger.couldNotTransform(transformation: Transformation, nodeRefs: List<NodeRef>, exception: Throwable) {
    if (exception.cause != null) {
        error(
            "Couldn't transform <{}> nodes <{}>",
            transformation,
            nodeRefs,
            exception
        )
    } else {
        error(
            "Couldn't transform <{}> nodes <{}>\n> {}",
            transformation,
            nodeRefs,
            exception.toString()
        )
    }
}

fun Logger.logOnRetry(transformation: Transformation, nodeRefs: List<NodeRef>, attempt: Long, maxAttempts: Long, nextAttemptDelay: Duration) {
    warn(
        "Attempt ({}/{}). Transformation <{}> nodes <{}> will be run after <{}>",
        attempt,
        maxAttempts,
        transformation,
        nodeRefs,
        nextAttemptDelay.toPrettyString()
    )
}

private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
    String.format("%.3f", (millisEnd - millisStart) / 1000.0)