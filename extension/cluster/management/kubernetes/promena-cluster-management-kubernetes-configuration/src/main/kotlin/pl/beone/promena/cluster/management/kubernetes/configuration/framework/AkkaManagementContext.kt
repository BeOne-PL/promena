package pl.beone.promena.cluster.management.kubernetes.configuration.framework

import akka.actor.ActorSystem
import akka.management.javadsl.AkkaManagement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AkkaManagementContext {

    @Bean
    fun akkaManagement(
        actorSystem: ActorSystem
    ): AkkaManagement =
        AkkaManagement.get(actorSystem).apply {
            start()
        }


}