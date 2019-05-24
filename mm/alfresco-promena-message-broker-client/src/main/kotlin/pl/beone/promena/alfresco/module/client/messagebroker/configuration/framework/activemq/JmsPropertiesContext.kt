package pl.beone.promena.alfresco.module.client.messagebroker.configuration.framework.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.boot.autoconfigure.jms.JmsProperties
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getPropertyWithResolvedPlaceholders
import java.util.*

@Configuration
class JmsPropertiesContext {

    @Bean
    fun jmsProperties(@Qualifier("global-properties") properties: Properties) =
            JmsProperties().apply {
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.cache.consumers")
                        ?.toBoolean()?.let { cache.isConsumers = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.cache.producers")
                        ?.toBoolean()?.let { cache.isProducers = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.cache.session-cache-size")
                        ?.toInt()?.let { cache.sessionCacheSize = it }

                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.pub-sub-domain")
                        ?.toBoolean()?.let { isPubSubDomain = it }

                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.listener.acknowledge-mode")
                        ?.let { listener.acknowledgeMode = JmsProperties.AcknowledgeMode.valueOf(it) }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.listener.auto-startup")
                        ?.toBoolean()?.let { listener.isAutoStartup = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.listener.concurrency")
                        ?.toInt()?.let { listener.concurrency = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.listener.max-concurrency")
                        ?.toInt()?.let { listener.maxConcurrency = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.listener.max-concurrency")
                        ?.toInt()?.let { listener.maxConcurrency = it }
            }
}