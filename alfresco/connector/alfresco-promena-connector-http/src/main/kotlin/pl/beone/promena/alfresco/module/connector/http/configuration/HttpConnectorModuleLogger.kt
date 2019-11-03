package pl.beone.promena.alfresco.module.connector.http.configuration

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import java.util.*
import javax.annotation.PostConstruct

@Configuration
@DependsOn("httpPromenaTransformer")
class HttpConnectorModuleLogger(
    @Qualifier("global-properties") private val properties: Properties
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Registered <http> connector: [<host: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.host")}>, " +
                    "<port: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.port")}>, " +
                    "<execution.threads: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.execution.threads")}>]"
        }
    }
}