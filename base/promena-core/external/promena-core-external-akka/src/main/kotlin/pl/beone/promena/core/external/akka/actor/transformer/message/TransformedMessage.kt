package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

data class TransformedMessage(val transformedDataDescriptors: List<TransformedDataDescriptor>)