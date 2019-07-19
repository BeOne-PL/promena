package pl.beone.promena.core.external.akka.actor.serializer.message

import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor

data class DeserializedMessage(val transformationDescriptor: TransformationDescriptor)