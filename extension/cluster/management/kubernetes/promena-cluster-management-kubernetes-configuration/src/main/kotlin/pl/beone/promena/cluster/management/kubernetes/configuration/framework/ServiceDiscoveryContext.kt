package pl.beone.promena.cluster.management.kubernetes.configuration.framework

import akka.actor.ActorSystem
import akka.discovery.Discovery
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceDiscoveryContext {

    @Bean
    fun serviceDiscovery(
        actorSystem: ActorSystem
    ): Discovery =
        Discovery.get(actorSystem).apply {
            loadServiceDiscovery("kubernetes-api")
        }

}