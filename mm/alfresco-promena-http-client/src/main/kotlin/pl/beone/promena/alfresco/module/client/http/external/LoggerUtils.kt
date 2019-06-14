package pl.beone.promena.alfresco.module.client.http.external

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.Logger
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

internal fun Logger.startSync(transformerId: String,
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

internal fun Logger.startAsync(transformerId: String,
                               nodeRefs: List<NodeRef>,
                               targetMediaType: MediaType,
                               parameters: Parameters) {
    info("Transforming <{}> <{}> nodes <{}> to <{}>...",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType)
}

internal fun Logger.transformedSuccessful(transformerId: String,
                                          nodeRefs: List<NodeRef>,
                                          targetMediaType: MediaType,
                                          parameters: Parameters,
                                          targetNodeRefs: List<NodeRef>,
                                          millisStart: Long) {
    info("Transformed <{}> <{}> nodes <{}> to <{}> <{}> in <{} s>",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         targetNodeRefs,
         calculateExecutionTimeInSeconds(millisStart, System.currentTimeMillis()))
}

internal fun Logger.skippedSavingResult(transformerId: String,
                                        nodeRefs: List<NodeRef>,
                                        targetMediaType: MediaType,
                                        parameters: Parameters,
                                        exception: NodesInconsistencyException) {
    warn("Skipped saving result <{}> transformation <{}> nodes <{}> to <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
         transformerId,
         parameters,
         nodeRefs,
         targetMediaType,
         exception.oldNodesChecksum,
         exception.currentNodesChecksum)
}

internal fun Logger.couldNotTransformButChecksumsAreDifferent(transformerId: String,
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

internal fun Logger.couldNotTransform(transformerId: String,
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

private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
        String.format("%.3f", (millisEnd - millisStart) / 1000.0)