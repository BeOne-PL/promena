package pl.beone.promena.core.contract.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.contract.transformer.TransformerId

interface ActorGetter {

    @Throws(TransformerNotFoundException::class)
    fun get(transformationTransformerId: TransformerId): ActorRef
}