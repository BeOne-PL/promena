package pl.beone.promena.core.configuration.external.akka.framework.logging

import akka.actor.ActorSystem
import akka.event.Logging
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class ApplyAkkaDebugLogging(
    private val actorSystem: ActorSystem
) {

    // apply DEBUG because everything is controlled by Spring Boot logging
    @PostConstruct
    private fun execute() {
        actorSystem.eventStream.setLogLevel(Logging.DebugLevel())
    }
}