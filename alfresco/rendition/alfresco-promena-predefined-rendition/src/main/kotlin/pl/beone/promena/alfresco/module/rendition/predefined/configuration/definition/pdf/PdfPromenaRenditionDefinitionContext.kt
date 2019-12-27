package pl.beone.promena.alfresco.module.rendition.predefined.configuration.definition.pdf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.predefined.internal.definition.pdf.PdfPromenaRenditionDefinition

@Configuration
class PdfPromenaRenditionDefinitionContext {

    @Bean
    fun pdfPromenaRenditionDefinition() =
        PdfPromenaRenditionDefinition()
}