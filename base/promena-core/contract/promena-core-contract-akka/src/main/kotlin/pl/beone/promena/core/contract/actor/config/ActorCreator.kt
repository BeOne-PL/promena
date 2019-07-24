package pl.beone.promena.core.contract.actor.config

import akka.actor.Props
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithTransformerId

interface ActorCreator {

    fun create(transformerId: String, props: Props, actors: Int): ActorRefWithTransformerId
}