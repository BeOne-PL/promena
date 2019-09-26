package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.GenericContainer

@Configuration
class ActiveMQContainerContext {

    @Bean
    fun activeMQContainer(): GenericContainer<Nothing> =
        GenericContainer<Nothing>("rmohr/activemq:5.15.6-alpine").apply {
            start()
        }
}