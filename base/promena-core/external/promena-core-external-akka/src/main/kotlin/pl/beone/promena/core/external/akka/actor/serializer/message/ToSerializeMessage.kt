package pl.beone.promena.core.external.akka.actor.serializer.message

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

data class ToSerializeMessage(val transformedDataDescriptors: TransformedDataDescriptors)