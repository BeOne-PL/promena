package pl.beone.promena.core.applicationmodel.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.transformer.contract.transformer.TransformerId

/**
 * Provides a full description of a transformer actor.
 */
data class TransformerActorDescriptor(
    val transformerId: TransformerId,
    val actorRef: ActorRef,
    val actors: Int
)