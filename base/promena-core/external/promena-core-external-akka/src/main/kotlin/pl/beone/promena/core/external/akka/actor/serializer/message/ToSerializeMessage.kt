package pl.beone.promena.core.external.akka.actor.serializer.message

import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

data class ToSerializeMessage(val transformedDataDescriptors: List<TransformedDataDescriptor>)