package pl.beone.promena.core.external.akka.actor.transformer

import pl.beone.promena.transformer.contract.Transformer

internal class TransformerExceptionsAccumulator {

    private data class TransformerAndReason(val transformer: Transformer,
                                            val reason: String)

    private val transformerAndReasonList = ArrayList<TransformerAndReason>()

    fun add(transformer: Transformer, reason: String) {
        transformerAndReasonList.add(TransformerAndReason(transformer, reason))
    }

    fun generateDescription(): String =
        "[" +
        transformerAndReasonList.joinToString(", ") { (transformer, reason) -> "<${transformer.javaClass.canonicalName}, $reason>" } +
        "]"
}