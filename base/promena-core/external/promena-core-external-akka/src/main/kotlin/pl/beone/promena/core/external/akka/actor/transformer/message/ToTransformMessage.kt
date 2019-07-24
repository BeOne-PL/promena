package pl.beone.promena.core.external.akka.actor.transformer.message

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.TransformerId

data class ToTransformMessage(val transformerId: TransformerId,
                              val dataDescriptor: DataDescriptor,
                              val targetMediaType: MediaType,
                              val parameters: Parameters)