package pl.beone.promena.connector.http.configuration.delivery.http

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.http.delivery.http.TransformerHandler
import pl.beone.promena.connector.http.delivery.http.route

@Configuration
class TransformerRouterContext {

    @Bean
    fun transformerRouter(
        handler: TransformerHandler
    ) =
        route(handler)
}