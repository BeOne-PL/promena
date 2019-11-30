package pl.beone.promena.alfresco.module.rendition.predefined.configuration.definition.pdf

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.rendition.predefined.internal.definition.pdf.PdfPromenaRenditionDefinition
import java.util.*

@Configuration
class PdfPromenaRenditionDefinitionContext {

    @Bean
    fun pdfPromenaRenditionDefinition(
        @Qualifier("global-properties") properties: Properties
    ) =
        PdfPromenaRenditionDefinition(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.predefined.rendition.pdf.apply-for-images").toBoolean()
        )
}