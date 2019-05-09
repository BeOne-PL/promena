package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

data class ToTransformMessage(val dataDescriptors: List<DataDescriptor>,
                              val targetMediaType: MediaType,
                              val parameters: Parameters)