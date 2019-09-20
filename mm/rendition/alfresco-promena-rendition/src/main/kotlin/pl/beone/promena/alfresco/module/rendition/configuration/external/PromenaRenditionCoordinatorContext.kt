package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.alfresco.service.cmr.repository.NodeService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.client.base.extension.getPropertyWithEmptySupport
import pl.beone.promena.alfresco.module.client.base.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.base.extension.toDuration
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionCoordinator
import java.util.*

@Configuration
class PromenaRenditionCoordinatorContext {

    @Bean
    fun promenaRenditionCoordinator(
        applicationContext: ApplicationContext,
        @Qualifier("global-properties") properties: Properties,
        nodeService: NodeService,
        promenaAlfrescoRenditionDefinitionGetter: PromenaAlfrescoRenditionDefinitionGetter
    ) =
        PromenaRenditionCoordinator(
            nodeService,
            promenaAlfrescoRenditionDefinitionGetter,
            applicationContext.getAlfrescoPromenaTransformer(properties.getPropertyWithEmptySupport("promena.rendition.transformer.bean.name")),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.rendition.transformation.timeout").toDuration()
        )

    private fun ApplicationContext.getAlfrescoPromenaTransformer(beanName: String?): AlfrescoPromenaTransformer =
        if (beanName != null) {
            getBean(beanName, AlfrescoPromenaTransformer::class.java)
        } else {
            getBean(AlfrescoPromenaTransformer::class.java)
        }
}