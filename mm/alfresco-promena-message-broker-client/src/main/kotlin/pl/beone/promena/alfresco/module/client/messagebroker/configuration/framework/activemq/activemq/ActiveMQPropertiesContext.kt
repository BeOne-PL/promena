package pl.beone.promena.alfresco.module.client.messagebroker.configuration.framework.activemq.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.boot.autoconfigure.jms.activemq.ActiveMQProperties
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getPropertyWithResolvedPlaceholders
import java.time.Duration
import java.util.*

@Configuration
class ActiveMQPropertiesContext {

    @Bean
    fun activeMQProperties(@Qualifier("global-properties") properties: Properties) =
            ActiveMQProperties().apply {
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.broker-url")
                        ?.let { brokerUrl = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.close-timeout")
                        ?.let { closeTimeout = Duration.parse(it) }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.in-memory")
                        ?.toBoolean()?.let { isInMemory = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.non-blocking-redelivery")
                        ?.toBoolean()?.let { isNonBlockingRedelivery = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.password")
                        ?.let { password = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.send-timeout")
                        ?.let { sendTimeout = Duration.parse(it) }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.user")
                        ?.let { user = it }

                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.packages.trust-all")
                        ?.toBoolean()?.let { packages.trustAll = it }
                properties.getPropertyWithResolvedPlaceholders("promena.client.message-broker.spring.activemq.packages.trusted")
                        ?.let { packages.trusted = it.split(",") }
            }
}