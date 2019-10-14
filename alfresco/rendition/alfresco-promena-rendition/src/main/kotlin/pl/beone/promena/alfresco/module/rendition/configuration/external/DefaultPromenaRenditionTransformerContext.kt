package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.alfresco.service.cmr.repository.ContentService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.PromenaTransformer
import pl.beone.promena.alfresco.module.core.extension.getPropertyWithEmptySupport
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.extension.toDuration
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter
import pl.beone.promena.alfresco.module.rendition.external.DefaultPromenaRenditionTransformer
import java.util.*

@Configuration
class DefaultPromenaRenditionTransformerContext {

    @Bean
    fun defaultPromenaRenditionTransformer(
        applicationContext: ApplicationContext,
        @Qualifier("global-properties") properties: Properties,
        contentService: ContentService,
        renditionGetter: RenditionGetter,
        promenaRenditionInProgressSynchronizer: PromenaRenditionInProgressSynchronizer,
        promenaRenditionDefinitionGetter: PromenaRenditionDefinitionGetter
    ) =
        DefaultPromenaRenditionTransformer(
            contentService,
            renditionGetter,
            promenaRenditionDefinitionGetter,
            promenaRenditionInProgressSynchronizer,
            applicationContext.getAlfrescoPromenaTransformer(properties.getPropertyWithEmptySupport("promena.rendition.transformer.bean.name")),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.rendition.transformation.timeout").toDuration()
        )

    private fun ApplicationContext.getAlfrescoPromenaTransformer(beanName: String?): PromenaTransformer =
        if (beanName != null) getBean(beanName, PromenaTransformer::class.java) else getBean(PromenaTransformer::class.java)
}