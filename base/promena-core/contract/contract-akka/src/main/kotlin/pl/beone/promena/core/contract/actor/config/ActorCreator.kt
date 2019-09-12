package pl.beone.promena.core.contract.actor.config

import akka.actor.ActorRef
import akka.actor.Props

interface ActorCreator {

    fun create(name: String, props: Props, actors: Int): ActorRef
}