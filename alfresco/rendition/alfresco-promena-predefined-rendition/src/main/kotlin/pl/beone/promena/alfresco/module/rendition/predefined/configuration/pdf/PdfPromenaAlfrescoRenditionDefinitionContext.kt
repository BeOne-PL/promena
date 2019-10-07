package pl.beone.promena.alfresco.module.rendition.predefined.configuration.pdf

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf.PdfPromenaAlfrescoRenditionDefinition
import java.util.*

@Configuration
class PdfPromenaAlfrescoRenditionDefinitionContext {

    @Bean
    fun pdfPromenaAlfrescoRenditionDefinition(
        @Qualifier("global-properties") properties: Properties
    ) =
        PdfPromenaAlfrescoRenditionDefinition(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.predefined.rendition.pdf.not-apply-for-images").toBoolean()
        )
}