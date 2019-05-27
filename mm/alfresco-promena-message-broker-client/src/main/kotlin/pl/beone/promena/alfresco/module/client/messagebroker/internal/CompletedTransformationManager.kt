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
    private val exceptionMap = ConcurrentHashMap<String, Exception>()

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

        return nodeRefsMap.remove(id)
               ?: exceptionMap.remove(id)?.let { throw it }
               ?: throw RuntimeException("Node refs for transaction <$id> aren't available")
    }

    fun completeTransformation(id: String, nodeRefs: List<NodeRef>) {
        completeTransformation(id) { nodeRefsMap[id] = nodeRefs }
    }

    fun completeErrorTransformation(id: String, exception: Exception) {
        completeTransformation(id) { exceptionMap[id] = exception }
    }

    private fun completeTransformation(id: String, toRun: () -> Unit) {
        val semaphore = semaphoresMap[id]

        if (semaphore != null) {
            toRun()
            semaphore.release()
        } else {
            logger.debug("Couldn't mark transformation <{}> as complete. Semaphore was removed earlier", id)
        }
    }
}