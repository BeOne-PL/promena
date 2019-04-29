package pl.beone.promena.module.http.client.configuration.external.httpclient.request

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.lib.http.client.external.httpclient.HttpTransformationExecutor
import java.util.*

@Configuration
class HttpTransformationExecutorContext {

    @Bean
    fun httpTransformationExecutor(@Qualifier("global-properties") properties: Properties) =
            HttpTransformationExecutor(properties.getProperty("promena.protocol"),
                                       properties.getProperty("promena.host"),
                                       properties.getProperty("promena.port").toInt(),
                                       properties.getProperty("promena.request.connectionTimeout").toInt(),
                                       properties.getProperty("promena.request.maxConnections").toInt())
}