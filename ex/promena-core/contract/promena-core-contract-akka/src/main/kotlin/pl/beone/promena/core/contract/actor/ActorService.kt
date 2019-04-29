package pl.beone.promena.core.contract.actor

import akka.actor.ActorRef
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException

interface ActorService {

    @Throws(TransformerNotFoundException::class)
    fun getTransformationActor(transformerId: String): ActorRef

    fun getSerializerActor(): ActorRef

}