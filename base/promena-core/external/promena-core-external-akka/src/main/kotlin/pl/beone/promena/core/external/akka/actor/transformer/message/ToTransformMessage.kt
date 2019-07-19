package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.model.Parameters

data class ToTransformMessage(val dataDescriptors: DataDescriptors,
                              val targetMediaType: MediaType,
                              val parameters: Parameters)