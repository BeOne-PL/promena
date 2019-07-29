package pl.beone.promena.alfresco.module.client.base.configuration.internal

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.client.base.configuration.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.base.configuration.toDuration
import java.util.*

@Configuration
class RetryContext {

    @Bean
    fun retry(@Qualifier("global-properties") properties: Properties) =
        if (properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.enabled").toBoolean()) {
            customRetry(
                properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.max-attempts").toLong(),
                properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.next-attempt-delay").toDuration()
            )
        } else {
            noRetry()
        }
}