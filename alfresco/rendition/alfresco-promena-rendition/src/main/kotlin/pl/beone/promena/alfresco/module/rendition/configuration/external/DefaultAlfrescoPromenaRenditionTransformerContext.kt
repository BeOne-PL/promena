package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.alfresco.service.cmr.repository.ContentService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.core.extension.getPropertyWithEmptySupport
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.extension.toDuration
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionInProgressSynchronizer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter
import pl.beone.promena.alfresco.module.rendition.external.DefaultAlfrescoPromenaRenditionTransformer
import java.util.*

@Configuration
class DefaultAlfrescoPromenaRenditionTransformerContext {

    @Bean
    fun defaultAlfrescoPromenaRenditionTransformer(
        applicationContext: ApplicationContext,
        @Qualifier("global-properties") properties: Properties,
        contentService: ContentService,
        alfrescoRenditionGetter: AlfrescoRenditionGetter,
        alfrescoPromenaRenditionInProgressSynchronizer: AlfrescoPromenaRenditionInProgressSynchronizer,
        alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter
    ) =
        DefaultAlfrescoPromenaRenditionTransformer(
            contentService,
            alfrescoRenditionGetter,
            alfrescoPromenaRenditionDefinitionGetter,
            alfrescoPromenaRenditionInProgressSynchronizer,
            applicationContext.getAlfrescoPromenaTransformer(properties.getPropertyWithEmptySupport("promena.rendition.transformer.bean.name")),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.rendition.transformation.timeout").toDuration()
        )

    private fun ApplicationContext.getAlfrescoPromenaTransformer(beanName: String?): AlfrescoPromenaTransformer =
        if (beanName != null) getBean(beanName, AlfrescoPromenaTransformer::class.java) else getBean(AlfrescoPromenaTransformer::class.java)
}