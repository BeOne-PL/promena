package pl.beone.promena.connector.messagebroker.integrationtest.container

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer

@Configuration
class ActiveMQContainerContext {

    @Bean
    fun activeMQContainer(): GenericContainer<Nothing> =
            FixedHostPortGenericContainer<Nothing>("rmohr/activemq:5.15.6-alpine").apply {
                withFixedExposedPort(61616, 61616)
                start()
            }
}