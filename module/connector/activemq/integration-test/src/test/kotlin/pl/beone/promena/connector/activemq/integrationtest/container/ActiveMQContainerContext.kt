package pl.beone.promena.connector.activemq.integrationtest.container

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.FixedHostPortGenericContainer

@Configuration
class ActiveMQContainerContext {

    @Bean
    fun activeMQContainer(): FixedHostPortGenericContainer<Nothing> =
        FixedHostPortGenericContainer<Nothing>("rmohr/activemq:5.15.6-alpine").apply {
            withFixedExposedPort(38204, 61616)
            start()
        }
}