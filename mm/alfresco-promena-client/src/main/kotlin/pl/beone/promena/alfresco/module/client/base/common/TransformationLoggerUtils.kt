package pl.beone.promena.alfresco.module.client.base.common

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

fun Logger.startSync(transformation: Transformation,
                     nodeRefs: List<NodeRef>,
                     waitMax: Duration?) {
    info("Transforming <{}> nodes <{}>. Waiting <{}> for response...",
         transformation,
         nodeRefs,
         waitMax)
}

fun Logger.startAsync(transformation: Transformation,
                      nodeRefs: List<NodeRef>) {
    info("Transforming <{}> nodes <{}>...",
         transformation,
         nodeRefs)
}

fun Logger.transformedSuccessfully(transformation: Transformation,
                                   nodeRefs: List<NodeRef>,
                                   targetNodeRefs: List<NodeRef>,
                                   startTimestamp: Long,
                                   endTimestamp: Long) {
    info("Transformed <{}> nodes <{}> to <{}> in <{} s>",
         transformation,
         nodeRefs,
         targetNodeRefs,
         calculateExecutionTimeInSeconds(startTimestamp, endTimestamp))
}

fun Logger.skippedSavingResult(transformation: Transformation,
                               nodeRefs: List<NodeRef>,
                               oldNodesChecksum: String,
                               currentNodesChecksum: String) {
    warn("Skipped saving result <{}> nodes <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
         transformation,
         nodeRefs,
         oldNodesChecksum,
         currentNodesChecksum)
}

fun Logger.couldNotTransformButChecksumsAreDifferent(transformation: Transformation,
                                                     nodeRefs: List<NodeRef>,
                                                     oldNodesChecksum: String,
                                                     currentNodesChecksum: String,
                                                     exception: Throwable) {
    warn("Couldn't transform <{}> nodes <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
         transformation,
         nodeRefs,
         oldNodesChecksum,
         currentNodesChecksum,
         exception)
}

fun Logger.couldNotTransform(transformation: Transformation,
                             nodeRefs: List<NodeRef>,
                             exception: Throwable) {
    error("Couldn't transform <{}> nodes <{}>",
          transformation,
          nodeRefs,
          exception)
}

fun Logger.logOnRetry(attempt: Long,
                      retryOnErrorMaxAttempts: Long,
                      transformation: Transformation,
                      nodeRefs: List<NodeRef>,
                      duration: Duration) {
    warn("Attempt ({}/{}). Transformation <{}> nodes <{}> will be run after <{}>",
         attempt,
         retryOnErrorMaxAttempts,
         transformation,
         nodeRefs,
         duration)
}

fun Logger.logOnRetry(attempt: Long,
                      retryOnErrorMaxAttempts: Long,
                      transformation: Transformation,
                      nodeRefs: List<NodeRef>) {
    warn("Attempt ({}/{}). Transforming <{}> nodes <{}>...",
         attempt,
         retryOnErrorMaxAttempts,
         transformation,
         nodeRefs)
}

private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
    String.format("%.3f", (millisEnd - millisStart) / 1000.0)