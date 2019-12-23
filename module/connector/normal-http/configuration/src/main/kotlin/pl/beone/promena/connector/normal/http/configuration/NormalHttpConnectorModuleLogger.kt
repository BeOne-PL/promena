package pl.beone.promena.connector.normal.http.configuration

import mu.KotlinLogging
import org.springframework.boot.logging.LogLevel
import org.springframework.boot.logging.LogLevel.*
import org.springframework.boot.logging.LoggingSystem
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import javax.annotation.PostConstruct

@Configuration
@DependsOn("normalTransformerController")
class NormalHttpConnectorModuleLogger(
    private val environment: Environment,
    private val loggingSystem: LoggingSystem
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info { "Registered <normal-http> connector: [<port: ${environment.getRequiredProperty("server.port")}>]" }

        // Disable standard logging. Exceptions are handled in NormalTransformerController so there is no need to print exception to logs
        loggingSystem.setLogLevel("org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler", OFF)
        loggingSystem.setLogLevel("org.springframework.web.HttpLogging", OFF)
    }
}