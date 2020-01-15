package pl.beone.promena.core.contract.actor

import akka.actor.ActorRef
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.contract.transformer.TransformerId

interface TransformerActorGetter {

    /**
     * @throws TransformerNotFoundException if there is no [transformationTransformerId] transformer actor
     */
    fun get(transformationTransformerId: TransformerId): ActorRef
}