package pl.beone.promena.alfresco.module.client.base.common

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

fun Logger.startSync(transformerId: String,
                     nodeRefs: List<NodeRef>,
                     targetMediaType: MediaType,
                     parameters: Parameters,
                     waitMax: Duration?) {
    info("Transforming <{}> <{}> nodes <{}> to <{}>. Waiting <{}> for response...",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         waitMax)
}

fun Logger.startAsync(transformerId: String,
                      nodeRefs: List<NodeRef>,
                      targetMediaType: MediaType,
                      parameters: Parameters) {
    info("Transforming <{}> <{}> nodes <{}> to <{}>...",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType)
}

fun Logger.transformedSuccessfully(transformerId: String,
                                   nodeRefs: List<NodeRef>,
                                   targetMediaType: MediaType,
                                   parameters: Parameters,
                                   targetNodeRefs: List<NodeRef>,
                                   startTimestamp: Long,
                                   endTimestamp: Long) {
    info("Transformed <{}> <{}> nodes <{}> to <{}> <{}> in <{} s>",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         targetNodeRefs,
         calculateExecutionTimeInSeconds(startTimestamp, endTimestamp))
}

fun Logger.skippedSavingResult(transformerId: String,
                               nodeRefs: List<NodeRef>,
                               targetMediaType: MediaType,
                               parameters: Parameters,
                               oldNodesChecksum: String,
                               currentNodesChecksum: String) {
    warn("Skipped saving result <{}> transformation <{}> nodes <{}> to <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         oldNodesChecksum,
         currentNodesChecksum)
}

fun Logger.couldNotTransformButChecksumsAreDifferent(transformerId: String,
                                                     nodeRefs: List<NodeRef>,
                                                     targetMediaType: MediaType,
                                                     parameters: Parameters,
                                                     nodesChecksum: String,
                                                     currentNodesChecksum: String,
                                                     exception: Throwable) {
    warn("Couldn't transform <{}> <{}> nodes <{}> to <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         nodesChecksum,
         currentNodesChecksum,
         exception)
}

fun Logger.couldNotTransform(transformerId: String,
                             nodeRefs: List<NodeRef>,
                             targetMediaType: MediaType,
                             parameters: Parameters,
                             exception: Throwable) {
    error("Couldn't transform <{}> <{}> nodes <{}> to <{}>",
          transformerId,
          parameters,
          nodeRefs,
          targetMediaType,
          exception)
}

fun Logger.logOnRetry(attempt: Long,
                      retryOnErrorMaxAttempts: Long,
                      transformerId: String,
                      parameters: Parameters,
                      nodeRefs: List<NodeRef>,
                      targetMediaType: MediaType,
                      duration: Duration) {
    warn("Attempt ({}/{}). Transformation <{}> <{}> nodes <{}> to <{}> will be run after <{}>",
         attempt,
         retryOnErrorMaxAttempts,
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         duration)
}

fun Logger.logOnRetry(attempt: Long,
                      retryOnErrorMaxAttempts: Long,
                      transformerId: String,
                      parameters: Parameters,
                      nodeRefs: List<NodeRef>,
                      targetMediaType: MediaType) {
    warn("Attempt ({}/{}). Transforming <{}> <{}> nodes <{}> to <{}>...",
         attempt,
         retryOnErrorMaxAttempts,
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType)
}

private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
        String.format("%.3f", (millisEnd - millisStart) / 1000.0)