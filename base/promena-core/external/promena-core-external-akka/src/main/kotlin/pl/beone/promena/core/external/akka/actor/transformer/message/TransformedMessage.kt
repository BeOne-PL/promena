package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

data class TransformedMessage(val transformedDataDescriptors: TransformedDataDescriptors)