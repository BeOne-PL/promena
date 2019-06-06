package pl.beone.promena.alfresco.module.client.base.configuration.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.configuration.getAndVerifyLocation
import pl.beone.promena.alfresco.module.client.base.external.FileAlfrescoDataConverter
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
