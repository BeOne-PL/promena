package pl.beone.promena.alfresco.module.client.messagebroker.internal

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class CompletedTransformationManager {

    companion object {
        private val logger = LoggerFactory.getLogger(CompletedTransformationManager::class.java)
    }

    private val semaphoresMap = ConcurrentHashMap<String, Semaphore>()
    private val nodeRefsMap = ConcurrentHashMap<String, List<NodeRef>>()

    fun startTransformation(id: String) {
        val semaphore = Semaphore(0)
        semaphoresMap[id] = semaphore
    }

    fun getTransformedNodeRefs(id: String, waitMax: Duration?): List<NodeRef> {
        val result = semaphoresMap[id]?.tryAcquire(waitMax?.toMillis() ?: Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                     ?: throw RuntimeException("Couldn't synchronize <$id> transaction")

        semaphoresMap.remove(id)

        if (!result) {
            throw TimeoutException()
        }

        return nodeRefsMap.remove(id) ?: throw RuntimeException("Node refs for transaction <$id> aren't available")
    }

    fun completeTransformation(id: String, nodeRefs: List<NodeRef>) {
        val semaphore = semaphoresMap[id]

        if (semaphore != null) {
            nodeRefsMap[id] = nodeRefs
            semaphore.release()
        } else {
            logger.debug("Couldn't mark transformation <{}> as complete. Semaphore was removed earlier", id)
        }
    }
}