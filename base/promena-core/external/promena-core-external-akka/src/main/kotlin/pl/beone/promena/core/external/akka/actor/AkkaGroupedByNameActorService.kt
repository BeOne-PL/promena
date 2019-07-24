package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithTransformerId
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.transformer.contract.transformer.TransformerId

class AkkaGroupedByNameActorService(private val actorRefWithTransformerIdList: List<ActorRefWithTransformerId>,
                                    private val serializerActorRef: ActorRef) : ActorService {

    override fun getTransformerActor(transformerId: TransformerId): ActorRef =
            actorRefWithTransformerIdList.firstOrNull { it.transformerId.name == transformerId.name }?.ref
                    ?: throw throw TransformerNotFoundException("There is no <${transformerId.name}> transformer")

    override fun getSerializerActor(): ActorRef =
            serializerActorRef
}