package pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration.external.akka.actor.config

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.metrics.AdaptiveLoadBalancingGroup
import akka.cluster.metrics.MetricsSelector
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.routing.SmallestMailboxPool
import pl.beone.promena.core.contract.actor.config.ActorCreator

class AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(private val actorSystem: ActorSystem,
                                                                  private val metricsSelector: MetricsSelector) : ActorCreator {

    override fun create(name: String, props: Props, actors: Int): ActorRef =
        actorSystem.actorOf(
                ClusterRouterGroup(AdaptiveLoadBalancingGroup(metricsSelector, emptySet()),
                                   ClusterRouterGroupSettings(Int.MAX_VALUE, listOf("/user/$name"), true, emptySet()))
                        .props(),
                "$name-router"
        )

    // TODO it should be use
    private fun createSmallestMailboxPool(transformerId: String, props: Props, actors: Int) {
        actorSystem.actorOf(SmallestMailboxPool(actors).props(props), transformerId)
    }

}