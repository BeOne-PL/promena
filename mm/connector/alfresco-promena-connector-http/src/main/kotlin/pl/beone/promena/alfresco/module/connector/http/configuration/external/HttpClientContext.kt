package pl.beone.promena.alfresco.module.connector.http.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import reactor.netty.http.client.HttpClient
import java.util.*

@Configuration
class HttpClientContext {

    @Bean
    fun httpClient(
        @Qualifier("global-properties") properties: Properties
    ) =
        HttpClient.create()
            .baseUrl(properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.base-url"))
}