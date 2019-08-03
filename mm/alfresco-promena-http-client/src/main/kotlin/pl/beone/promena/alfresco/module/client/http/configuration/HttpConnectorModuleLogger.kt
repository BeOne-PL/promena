package pl.beone.promena.alfresco.module.client.http.configuration

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import java.util.*
import javax.annotation.PostConstruct

@Configuration
@DependsOn("httpClient", "httpClientAlfrescoPromenaService")
class HttpConnectorModuleLogger(
    @Qualifier("global-properties") private val properties: Properties
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info { "Registered <http> connector: <base-url: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.http.base-url")}>" }
    }
}