package pl.beone.promena.module.http.client.configuration.external.alfresco

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.module.http.client.external.alfresco.AlfrescoFileDataConverter
import java.io.File
import java.net.URI
import java.util.*

@Configuration
class AlfrescoFileDataConverterContext {

    @Bean
    fun alfrescoFileDataConverter(@Qualifier("global-properties") properties: Properties) =
            AlfrescoFileDataConverter(properties.getAndVerifyLocation())
}

internal fun Properties.getAndVerifyLocation(): URI? {
    val property = this.getProperty("promena.communication.file.location") ?: return null

    if (property.isEmpty()) {
        return null
    }

    val uri = URI(property)

    try {
        File(uri).exists()
    } catch (e: Exception) {
        throw IllegalArgumentException("File location URI <$uri> isn't correct", e)
    }

    return uri
}