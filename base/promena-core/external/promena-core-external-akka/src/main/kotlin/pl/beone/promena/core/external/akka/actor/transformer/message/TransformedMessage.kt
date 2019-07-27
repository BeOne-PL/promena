package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

data class TransformedMessage(
    val transformedDataDescriptor: TransformedDataDescriptor
)