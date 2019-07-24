package pl.beone.promena.core.applicationmodel.akka.actor

import akka.actor.ActorRef
import pl.beone.promena.transformer.contract.transformer.TransformerId

data class ActorRefWithTransformerId(val ref: ActorRef,
                                     val transformerId: TransformerId)