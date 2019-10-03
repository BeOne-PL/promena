package pl.beone.promena.alfresco.module.connector.activemq.configuration.framework.activemq.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.configuration.autoconfigure.jms.activemq.ActiveMQProperties
import pl.beone.promena.alfresco.module.core.extension.getPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.extension.toDuration
import java.util.*

@Configuration
class ActiveMQPropertiesContext {

    @Bean
    fun activeMQProperties(@Qualifier("global-properties") properties: Properties) =
        ActiveMQProperties().apply {
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.broker-url")
                ?.let { brokerUrl = it }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.close-timeout")
                ?.let { closeTimeout = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.in-memory")
                ?.toBoolean()?.let { isInMemory = it }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.non-blocking-redelivery")
                ?.toBoolean()?.let { isNonBlockingRedelivery = it }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.password")
                ?.let { password = it }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.send-timeout")
                ?.let { sendTimeout = it.toDuration() }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.user")
                ?.let { user = it }

            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.packages.trust-all")
                ?.toBoolean()?.let { packages.trustAll = it }
            properties.getPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.packages.trusted")
                ?.let { packages.trusted = it.split(",") }
        }
}