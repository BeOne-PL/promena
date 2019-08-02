package pl.beone.promena.connector.http.configuration

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import javax.annotation.PostConstruct

@Configuration
class HttpConnectorModuleLogger(
    private val environment: Environment
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HttpConnectorModuleLogger::class.java)
    }

    @PostConstruct
    private fun log() {
        logger.info("Registered <http> connector: <port: {}>", environment.getRequiredProperty("server.port"))
    }
}