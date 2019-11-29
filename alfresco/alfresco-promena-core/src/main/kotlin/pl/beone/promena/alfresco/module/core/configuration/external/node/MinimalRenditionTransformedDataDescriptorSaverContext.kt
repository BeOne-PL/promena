package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.ServiceRegistry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataSaver
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.external.node.MinimalRenditionTransformedDataDescriptorSaver
import java.util.*

@Configuration
class MinimalRenditionTransformedDataDescriptorSaverContext {

    @Bean
    fun minimalRenditionTransformedDataDescriptorSaver(
        @Qualifier("global-properties") properties: Properties,
        promenaTransformationMetadataSavers: Optional<List<PromenaTransformationMetadataSaver>>,
        dataConverter: DataConverter,
        serviceRegistry: ServiceRegistry
    ) =
        MinimalRenditionTransformedDataDescriptorSaver(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.save-if-zero").toBoolean(),
            promenaTransformationMetadataSavers.orElse(emptyList()),
            dataConverter,
            serviceRegistry
        )
}