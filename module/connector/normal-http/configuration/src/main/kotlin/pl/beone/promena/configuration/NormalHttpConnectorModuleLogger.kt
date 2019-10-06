package pl.beone.promena.configuration

import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import javax.annotation.PostConstruct

@Configuration
@DependsOn("transformationNormalController")
class NormalHttpConnectorModuleLogger(
    private val environment: Environment
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info { "Registered <normal-http> connector: [<port: ${environment.getRequiredProperty("server.port")}>]" }
    }
}