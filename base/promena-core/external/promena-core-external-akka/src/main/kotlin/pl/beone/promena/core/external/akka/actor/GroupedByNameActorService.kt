package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.transformer.contract.transformer.TransformerId

class GroupedByNameActorService(private val transformerActorDescriptors: List<TransformerActorDescriptor>,
                                private val serializerActorRef: ActorRef) : ActorService {

    override fun getTransformerActor(transformationTransformerId: TransformerId): ActorRef =
        getTransformer(transformationTransformerId)?.actorRef
        ?: throw throw TransformerNotFoundException("There is no <${transformationTransformerId.toDescription()}> transformer")

    private fun getTransformer(transformerId: TransformerId): TransformerActorDescriptor? =
        if (transformerId.subName != null) {
            transformerActorDescriptors.firstOrNull { it.transformerId == transformerId }
        } else {
            transformerActorDescriptors.firstOrNull { it.transformerId.name == transformerId.name }
        }

    private fun TransformerId.toDescription(): String =
        if (subName == null) {
            name
        } else {
            "$name, $subName"
        }

    override fun getSerializerActor(): ActorRef =
        serializerActorRef

}
