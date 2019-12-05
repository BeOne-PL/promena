package pl.beone.promena.alfresco.module.core.configuration.external.transformation

import org.alfresco.service.ServiceRegistry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.extension.toDuration
import pl.beone.promena.alfresco.module.core.external.transformation.manager.MemoryWithAlfrescoPersistencePromenaMutableTransformationManager
import pl.beone.promena.core.contract.serialization.SerializationService
import java.util.*

@Configuration
class MemoryWithAlfrescoPersistencePromenaMutableTransformationManagerContext {

    @Bean
    fun memoryWithAlfrescoPersistencePromenaMutableTransformationManager(
        @Qualifier("global-properties") properties: Properties,
        serializationService: SerializationService,
        serviceRegistry: ServiceRegistry
    ) =
        MemoryWithAlfrescoPersistencePromenaMutableTransformationManager(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.manager.persist-in-alfresco").toBoolean(),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.manager.buffer-size").toInt(),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.manager.wait-max").toDuration(),
            serializationService,
            serviceRegistry
        )
}