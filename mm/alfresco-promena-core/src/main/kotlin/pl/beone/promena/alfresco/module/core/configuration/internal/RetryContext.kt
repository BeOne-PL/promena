package pl.beone.promena.alfresco.module.core.configuration.internal

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.extension.toDuration
import java.util.*

@Configuration
class RetryContext {

    @Bean
    fun retry(@Qualifier("global-properties") properties: Properties) =
        if (properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.error.retry.enabled").toBoolean()) {
            customRetry(
                properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.error.retry.max-attempts").toLong(),
                properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.error.retry.next-attempt-delay").toDuration()
            )
        } else {
            noRetry()
        }
}