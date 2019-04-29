package pl.beone.promena.core.external.akka.actor.serializer.message

import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor

data class DeserializedMessage(val transformationDescriptor: TransformationDescriptor)