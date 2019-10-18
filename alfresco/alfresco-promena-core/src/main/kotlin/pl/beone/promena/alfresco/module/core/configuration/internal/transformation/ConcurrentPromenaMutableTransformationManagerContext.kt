package pl.beone.promena.alfresco.module.core.configuration.internal.transformation

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.extension.toDuration
import pl.beone.promena.alfresco.module.core.internal.transformation.ConcurrentPromenaMutableTransformationManager
import java.util.*

@Configuration
class ConcurrentPromenaMutableTransformationManagerContext {

    @Bean
    fun concurrentPromenaMutableTransformationManager(
        @Qualifier("global-properties") properties: Properties
    ) =
        ConcurrentPromenaMutableTransformationManager(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.manager.buffer-size").toInt(),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.manager.wait-max").toDuration()
        )
}