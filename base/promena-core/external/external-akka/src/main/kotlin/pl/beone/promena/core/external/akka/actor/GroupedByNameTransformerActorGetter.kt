package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.contract.actor.TransformerActorGetter
import pl.beone.promena.transformer.contract.transformer.TransformerId
import pl.beone.promena.transformer.internal.extension.toPrettyString

/**
 * Manages [transformerActorDescriptors] in memory.
 */
class GroupedByNameTransformerActorGetter(
    private val transformerActorDescriptors: List<TransformerActorDescriptor>
) : TransformerActorGetter {

    override fun get(transformationTransformerId: TransformerId): ActorRef =
        getTransformer(transformationTransformerId)?.actorRef
            ?: throw throw TransformerNotFoundException("There is no <${transformationTransformerId.toPrettyString()}> transformer")

    private fun getTransformer(transformerId: TransformerId): TransformerActorDescriptor? =
        if (transformerId.isSubNameSet()) {
            transformerActorDescriptors.firstOrNull { it.transformerId == transformerId }
        } else {
            transformerActorDescriptors.firstOrNull { it.transformerId.name == transformerId.name }
        }
}
