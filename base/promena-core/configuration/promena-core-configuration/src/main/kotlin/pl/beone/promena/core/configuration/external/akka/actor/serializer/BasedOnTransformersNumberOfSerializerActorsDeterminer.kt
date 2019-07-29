package pl.beone.promena.core.configuration.external.akka.actor.serializer

import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor

internal class BasedOnTransformersNumberOfSerializerActorsDeterminer {

    fun determine(transformerActorDescriptors: List<TransformerActorDescriptor>): Int =
        transformerActorDescriptors
            .groupBy { it.transformerId.name }
            .map { transformersGroupedByName -> transformersGroupedByName.value.first().actors }
            .sum()
}