package pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration.external.akka.actor.config

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.metrics.AdaptiveLoadBalancingGroup
import akka.cluster.metrics.MetricsSelector
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.routing.SmallestMailboxPool
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.contract.actor.config.ActorCreator

class AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(private val actorSystem: ActorSystem,
                                                                  private val metricsSelector: MetricsSelector) : ActorCreator {

    override fun create(transformerId: String, props: Props, actors: Int): ActorRefWithId {
        createSmallestMailboxPool(transformerId, props, actors)

        val actorRef = createAdaptiveLoadBalancingGroupOnSmallestMailboxPoolPath(transformerId)

        return ActorRefWithId(actorRef, transformerId)

    }

    private fun createAdaptiveLoadBalancingGroupOnSmallestMailboxPoolPath(transformerId: String): ActorRef =
            actorSystem.actorOf(
                    ClusterRouterGroup(AdaptiveLoadBalancingGroup(metricsSelector, emptySet()),
                                       ClusterRouterGroupSettings(Int.MAX_VALUE, listOf("/user/$transformerId"), true, emptySet()))
                            .props(),
                    "$transformerId-router"
            )

    private fun createSmallestMailboxPool(transformerId: String, props: Props, actors: Int) {
        actorSystem.actorOf(SmallestMailboxPool(actors).props(props), transformerId)
    }

}