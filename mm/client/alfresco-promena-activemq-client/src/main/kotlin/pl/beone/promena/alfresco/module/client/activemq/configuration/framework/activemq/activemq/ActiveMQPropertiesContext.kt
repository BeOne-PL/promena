package pl.beone.promena.alfresco.module.client.activemq.configuration.framework.activemq.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.configuration.autoconfigure.jms.activemq.ActiveMQProperties
import pl.beone.promena.alfresco.module.client.base.extension.getPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.base.extension.toDuration
import java.util.*

@Configuration
class ActiveMQPropertiesContext {

    @Bean
    fun activeMQProperties(@Qualifier("global-properties") properties: Properties) =
        ActiveMQProperties().apply {
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.broker-url")
                ?.let { brokerUrl = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.close-timeout")
                ?.let { closeTimeout = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.in-memory")
                ?.toBoolean()?.let { isInMemory = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.non-blocking-redelivery")
                ?.toBoolean()?.let { isNonBlockingRedelivery = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.password")
                ?.let { password = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.send-timeout")
                ?.let { sendTimeout = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.user")
                ?.let { user = it }

            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.packages.trust-all")
                ?.toBoolean()?.let { packages.trustAll = it }
            properties.getPropertyWithResolvedPlaceholders("promena.client.activemq.spring.activemq.packages.trusted")
                ?.let { packages.trusted = it.split(",") }
        }
}