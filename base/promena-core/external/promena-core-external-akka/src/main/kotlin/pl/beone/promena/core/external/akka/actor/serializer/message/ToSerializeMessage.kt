package pl.beone.promena.core.external.akka.actor.serializer.message

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

data class ToSerializeMessage(val transformedDataDescriptor: TransformedDataDescriptor)