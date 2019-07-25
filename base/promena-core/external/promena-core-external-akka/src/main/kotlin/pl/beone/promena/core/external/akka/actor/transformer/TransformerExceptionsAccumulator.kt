package pl.beone.promena.core.external.akka.actor.transformer

import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId

internal class TransformerExceptionsAccumulator {

    private data class TransformerAndReason(val transformer: Transformer,
                                            val reason: String)

    private val transformerAndReasonList = ArrayList<TransformerAndReason>()

    fun add(transformer: Transformer, reason: String) {
        transformerAndReasonList.add(TransformerAndReason(transformer, reason))
    }

    fun addUnsuitable(transformerDescriptor: TransformerDescriptor, transformationTransformerId: TransformerId) {
        val transformerId = transformerDescriptor.transformerId
        transformerAndReasonList.add(TransformerAndReason(transformerDescriptor.transformer,
                                                          "Transformer <${transformerId.toDescription()}> isn't suitable for <${transformationTransformerId.toDescription()}>"))
    }

    private fun TransformerId.toDescription(): String =
        if (subName == null) {
            name
        } else {
            "$name, $subName"
        }

    fun generateDescription(): String =
        "[" +
        transformerAndReasonList.joinToString(", ") { (transformer, reason) -> "<${transformer.javaClass.canonicalName}, $reason>" } +
        "]"
}