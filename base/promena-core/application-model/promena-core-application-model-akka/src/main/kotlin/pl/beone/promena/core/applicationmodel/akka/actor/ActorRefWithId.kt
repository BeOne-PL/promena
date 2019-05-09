package pl.beone.promena.core.applicationmodel.akka.actor

import akka.actor.ActorRef

data class ActorRefWithId(val ref: ActorRef,
                          val id: String)