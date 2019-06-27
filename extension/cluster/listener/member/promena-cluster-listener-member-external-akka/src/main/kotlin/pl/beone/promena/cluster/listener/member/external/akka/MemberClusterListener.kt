package pl.beone.promena.cluster.listener.member.external.akka

import akka.actor.AbstractActor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.*
import akka.event.Logging

class MemberClusterListener : AbstractActor() {

    private val log = Logging.getLogger(context.system(), this)

    private val cluster = Cluster.get(context.system())

    override fun preStart() {
        cluster.subscribe(self(), initialStateAsEvents(), MemberEvent::class.java, UnreachableMember::class.java)
    }

    override fun postStop() {
        cluster.unsubscribe(self())
    }

    override fun createReceive(): Receive =
            receiveBuilder()
                    .match(MemberUp::class.java) { log.info("Member is Up: {}", it.member()) }
                    .match(UnreachableMember::class.java) { log.info("Member detected as unreachable: {}", it.member()) }
                    .match(MemberRemoved::class.java) { log.info("Member is Removed: {}", it.member()) }
                    .match(MemberEvent::class.java) {
                        // ignore
                    }
                    .build()
}
