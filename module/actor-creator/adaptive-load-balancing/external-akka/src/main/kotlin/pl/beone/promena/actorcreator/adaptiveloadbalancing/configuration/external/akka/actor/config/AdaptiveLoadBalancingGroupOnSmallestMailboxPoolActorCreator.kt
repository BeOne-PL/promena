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

/**
 * An actor is created in two stages:
 * - Cluster level - creates cluster router group of [AdaptiveLoadBalancingGroup] based on [metricsSelector] (if [clusterAware] is `true`)
 * - Local level - creates [actors] instances in [SmallestMailboxPool]
 */
class AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(
    private val actorSystem: ActorSystem,
    private val metricsSelector: MetricsSelector
) : ActorCreator {

    override fun create(name: String, props: Props, actors: Int, clusterAware: Boolean): ActorRef {
        val smallestMailboxPoolActorRef = createSmallestMailboxPool(name, props, actors)

        return if (clusterAware) {
            createAdaptiveLoadBalancingGroupOnSmallestMailboxPoolPath(name)
        } else {
            smallestMailboxPoolActorRef
        }
    }

    private fun createSmallestMailboxPool(name: String, props: Props, actors: Int): ActorRef =
        actorSystem.actorOf(SmallestMailboxPool(actors).props(props), name)

    private fun createAdaptiveLoadBalancingGroupOnSmallestMailboxPoolPath(name: String): ActorRef =
        actorSystem.actorOf(
            ClusterRouterGroup(
                AdaptiveLoadBalancingGroup(metricsSelector, emptySet()),
                ClusterRouterGroupSettings(Int.MAX_VALUE, listOf("/user/$name"), true, emptySet())
            ).props(),
            "$name-router"
        )

}