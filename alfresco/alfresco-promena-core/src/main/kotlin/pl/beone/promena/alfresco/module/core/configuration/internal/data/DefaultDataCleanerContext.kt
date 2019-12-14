package pl.beone.promena.alfresco.module.core.configuration.internal.data

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.internal.data.DefaultDataCleaner

@Configuration
class DefaultDataCleanerContext {

    @Bean
    fun defaultDataCleaner() =
        DefaultDataCleaner()
}