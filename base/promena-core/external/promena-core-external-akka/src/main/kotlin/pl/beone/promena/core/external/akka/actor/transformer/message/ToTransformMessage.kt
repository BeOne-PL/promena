package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

data class ToTransformMessage(val dataDescriptor: DataDescriptor,
                              val targetMediaType: MediaType,
                              val parameters: Parameters)