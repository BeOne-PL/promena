package pl.beone.promena.alfresco.module.client.activemq.configuration.framework.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.configuration.autoconfigure.jms.JmsProperties
import pl.beone.promena.alfresco.module.client.base.extension.getPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.base.extension.toDuration
import java.util.*

@Configuration
class JmsPropertiesContext {

    @Bean
    fun jmsProperties(@Qualifier("global-properties") properties: Properties) =
        JmsProperties().apply {
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.pub-sub-domain")
                ?.toBoolean()?.let { isPubSubDomain = it }

            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.listener.auto-startup")
                ?.toBoolean()?.let { listener.isAutoStartup = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.listener.acknowledge-mode")
                ?.let { listener.acknowledgeMode = JmsProperties.AcknowledgeMode.valueOf(it) }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.listener.concurrency")
                ?.toInt()?.let { listener.concurrency = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.listener.max-concurrency")
                ?.toInt()?.let { listener.maxConcurrency = it }

            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.default-destination")
                ?.let { template.defaultDestination = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.delivery-delay")
                ?.let { template.deliveryDelay = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.delivery-mode")
                ?.let { template.deliveryMode = JmsProperties.DeliveryMode.valueOf(it) }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.priority")
                ?.toInt()?.let { template.priority = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.receive-timeout")
                ?.let { template.receiveTimeout = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.time-to-live")
                ?.let { template.timeToLive = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.jms.template.qos-enabled")
                ?.toBoolean()?.let { template.qosEnabled = it }
        }
}