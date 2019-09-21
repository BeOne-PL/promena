package pl.beone.promena.core.external.akka.actor.transformer

import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor

internal class TransformerExceptionsAccumulator {

    private data class TransformerDescriptorAndReason(
        val transformerDescriptor: TransformerDescriptor,
        val reason: String
    )

    private val transformerAndReasonList = ArrayList<TransformerDescriptorAndReason>()

    fun add(transformerDescriptor: TransformerDescriptor, reason: String) {
        transformerAndReasonList.add(TransformerDescriptorAndReason(transformerDescriptor, reason))
    }

    fun generateDescription(): String =
        transformerAndReasonList.joinToString("\n")
        { (transformerDescriptor, reason) -> "> ${transformerDescriptor.transformer.javaClass.canonicalName}(${transformerDescriptor.transformerId.name}, ${transformerDescriptor.transformerId.subName}): $reason" }
}