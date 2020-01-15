package pl.beone.promena.core.contract.actor.config

import akka.actor.ActorRef
import akka.actor.Props

interface ActorCreator {

    /**
     * Creates [name] actor with [props] and with [actors] instances in the pool.
     * [clusterAware] indicates if the actor can delegate a task to another actor in the cluster.
     */
    fun create(name: String, props: Props, actors: Int, clusterAware: Boolean): ActorRef
}