package pl.beone.promena.alfresco.module.client.http.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.http.configuration.getRequiredPropertyWithResolvedPlaceholders
import reactor.netty.http.client.HttpClient
import java.util.*

@Configuration
class HttpClientContext {

    @Bean
    fun httpClient(
        @Qualifier("global-properties") properties: Properties
    ) =
        HttpClient.create()
            .baseUrl(properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.http.base-url"))
}