package pl.beone.promena.alfresco.module.client.messagebroker.configuration.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getAndVerifyLocation
import pl.beone.promena.alfresco.module.client.messagebroker.external.FileAlfrescoDataConverter
import java.io.File
import java.net.URI
import java.util.*

@Configuration
class FileAlfrescoDataConverterContext {

    companion object {
        private val logger = LoggerFactory.getLogger(FileAlfrescoDataConverterContext::class.java)
    }

    @Bean
    fun fileAlfrescoDataConverter(@Qualifier("global-properties") properties: Properties) =
            FileAlfrescoDataConverter(properties.getAndVerifyLocation(logger))
}
