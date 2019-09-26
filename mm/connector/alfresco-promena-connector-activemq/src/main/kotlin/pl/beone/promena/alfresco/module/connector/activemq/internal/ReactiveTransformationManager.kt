package pl.beone.promena.alfresco.module.connector.activemq.internal

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.NodeRef
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class ReactiveTransformationManager {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val monoMap = ConcurrentHashMap<String, Mono<List<NodeRef>>>()
    private val completableFutureMap = ConcurrentHashMap<String, CompletableFuture<List<NodeRef>>>()

    fun startTransformation(id: String): Mono<List<NodeRef>> {
        val mono = monoMap[id]
        return if (mono != null) {
            mono
        } else {
            val completableFuture = CompletableFuture<List<NodeRef>>()
                .apply { completableFutureMap[id] = this }

            Mono.fromFuture(completableFuture)
                .doOnCancel { }
                .apply {
                    monoMap[id] = this
                    subscribe({}, {}, {
                        monoMap.remove(id)
                        completableFutureMap.remove(id)
                    })
                }
        }
    }

    fun completeTransformation(id: String, nodeRefs: List<NodeRef>) {
        completeTransformation(id) { it.complete(nodeRefs) }
    }

    fun completeErrorTransformation(id: String, exception: Exception) {
        completeTransformation(id) { it.completeExceptionally(exception) }
    }

    private fun completeTransformation(id: String, toRun: (CompletableFuture<List<NodeRef>>) -> Unit) {
        val completableFuture = completableFutureMap[id]

        if (completableFuture != null) {
            toRun(completableFuture)
        } else {
            logger.warn { "Couldn't find transformation <$id>. User won't be informed about the end of this transformation" }
        }
    }
}