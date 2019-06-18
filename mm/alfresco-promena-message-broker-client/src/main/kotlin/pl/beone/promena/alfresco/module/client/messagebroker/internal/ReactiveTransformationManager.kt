package pl.beone.promena.alfresco.module.client.messagebroker.internal

import org.alfresco.service.cmr.repository.NodeRef
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class ReactiveTransformationManager {

    private val completableFutureMap = ConcurrentHashMap<String, CompletableFuture<List<NodeRef>>>()

    fun startTransformation(id: String): Mono<List<NodeRef>> {
        val completableFuture = CompletableFuture<List<NodeRef>>()
                .apply { completableFutureMap[id] = this }

        return Mono.fromFuture(completableFuture)
                .doOnCancel { }
                .apply { subscribe({}, {}, { completableFutureMap.remove(id) }) }
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
            throw RuntimeException("Couldn't finish <$id>. CompletableFuture was removed earlier")
        }
    }
}