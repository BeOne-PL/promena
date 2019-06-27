package pl.beone.promena.cluster.management.kubernetes.configuration.framework

import akka.actor.ExtendedActorSystem
import akka.management.cluster.bootstrap.ClusterBootstrap
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClusterBootstrapContext {

    @Bean
    fun clusterBootstrap(actorSystem: ExtendedActorSystem) =
            ClusterBootstrap(actorSystem).apply {
                start()
            }

}