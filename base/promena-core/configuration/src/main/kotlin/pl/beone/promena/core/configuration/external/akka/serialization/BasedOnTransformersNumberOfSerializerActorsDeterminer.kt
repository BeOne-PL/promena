package pl.beone.promena.core.configuration.external.akka.serialization

import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor

internal fun determine(transformerActorDescriptors: List<TransformerActorDescriptor>): Int =
    transformerActorDescriptors
        .groupBy { it.transformerId.name }
        .map { transformersGroupedByName -> transformersGroupedByName.value.first().actors }
        .sum()