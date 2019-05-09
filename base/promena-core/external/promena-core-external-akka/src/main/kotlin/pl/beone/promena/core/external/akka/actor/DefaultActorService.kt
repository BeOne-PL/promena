package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException

class DefaultActorService(private val actorRefWithTransformerIdList: List<ActorRefWithId>,
                          private val serializerActorRef: ActorRef) : ActorService {


    override fun getTransformationActor(transformerId: String): ActorRef =
            actorRefWithTransformerIdList.firstOrNull { it.id == transformerId }?.ref
                    ?: throw throw TransformerNotFoundException("There is no <$transformerId> transformer")

    override fun getSerializerActor(): ActorRef =
            serializerActorRef
}