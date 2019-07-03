package pl.beone.promena.alfresco.module.client.messagebroker.configuration.framework.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.autoconfigure.jms.JmsProperties
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.toDuration
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

                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.default-destination")
                        ?.let { template.defaultDestination = it }

                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.default-destination")
                        ?.let { template.defaultDestination = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.delivery-delay")
                        ?.let { template.deliveryDelay = it.toDuration() }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.delivery-mode")
                        ?.let { template.deliveryMode = JmsProperties.DeliveryMode.valueOf(it) }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.priority")
                        ?.toInt()?.let { template.priority = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.qos-enabled")
                        ?.toBoolean()?.let { template.qosEnabled = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.receive-timeout")
                        ?.let { template.receiveTimeout = it.toDuration() }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.jms.template.time-to-live")
                        ?.let { template.timeToLive = it.toDuration() }
            }
}