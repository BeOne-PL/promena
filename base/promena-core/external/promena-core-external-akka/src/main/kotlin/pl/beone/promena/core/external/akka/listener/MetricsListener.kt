package pl.beone.promena.core.external.akka.listener

import akka.actor.AbstractActor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.ClusterMetricsChanged
import akka.cluster.metrics.ClusterMetricsExtension
import akka.cluster.metrics.NodeMetrics
import akka.cluster.metrics.StandardMetrics
import akka.event.Logging

// TODO as prototype
class MetricsListener : AbstractActor() {
    private var log = Logging.getLogger(context.system(), this)
    private var cluster = Cluster.get(context.system())

    private val extension = ClusterMetricsExtension.get(context.system)

    override fun preStart() {
        extension.subscribe(self)
    }

    override fun postStop() {
        extension.unsubscribe(self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(ClusterMetricsChanged::class.java) {
                for (nodeMetrics in it.nodeMetrics) {
                    if (nodeMetrics.address() == cluster.selfAddress()) {
                        logHeap(nodeMetrics)
                        logCpu(nodeMetrics)
                    }
                }
            }
            .match(CurrentClusterState::class.java) {
                // Ignore.
            }
            .build()
    }

    private fun logHeap(nodeMetrics: NodeMetrics) {
        val heap = StandardMetrics.extractHeapMemory(nodeMetrics)
        if (heap != null) {
            log.debug("Used heap: {} MB", heap.used().toDouble() / 1024.0 / 1024.0)
        }
    }

    private fun logCpu(nodeMetrics: NodeMetrics) {
        val cpu = StandardMetrics.extractCpu(nodeMetrics)
        if (cpu != null && cpu.systemLoadAverage().isDefined) {
            log.debug("Load: {} ({} processors)", cpu.systemLoadAverage().get(), cpu.processors())
        }
    }
}
