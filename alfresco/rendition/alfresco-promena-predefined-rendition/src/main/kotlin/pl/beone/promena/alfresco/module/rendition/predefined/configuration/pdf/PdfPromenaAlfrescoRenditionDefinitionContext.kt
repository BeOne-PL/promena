package pl.beone.promena.alfresco.module.rendition.predefined.configuration.pdf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf.PdfPromenaAlfrescoRenditionDefinition

@Configuration
class PdfPromenaAlfrescoRenditionDefinitionContext {

    @Bean
    fun pdfPromenaAlfrescoRenditionDefinition() =
        PdfPromenaAlfrescoRenditionDefinition
}