package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.contract.actor.ActorGetter
import pl.beone.promena.transformer.contract.transformer.TransformerId

class GroupedByNameTransformerActorGetter(
    private val transformerActorDescriptors: List<TransformerActorDescriptor>
) : ActorGetter {

    override fun get(transformationTransformerId: TransformerId): ActorRef =
        getTransformer(transformationTransformerId)?.actorRef
            ?: throw throw TransformerNotFoundException("There is no <$transformationTransformerId> transformer")

    private fun getTransformer(transformerId: TransformerId): TransformerActorDescriptor? =
        if (transformerId.isSubNameSet()) {
            transformerActorDescriptors.firstOrNull { it.transformerId == transformerId }
        } else {
            transformerActorDescriptors.firstOrNull { it.transformerId.name == transformerId.name }
        }
}
