package pl.beone.promena.alfresco.module.client.messagebroker

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.*

@Configuration
class GlobalPropertiesContext(private val environment: Environment) {

    @Bean("global-properties")
    fun globalProperties(): Properties =
            Properties().apply {
                "promena.client.message-broker.waitMax".let { setProperty(it, environment.getProperty(it)) }

                "promena.client.message-broker.consumer.queue.request".let { setProperty(it, environment.getProperty(it)) }
                "promena.client.message-broker.consumer.queue.response".let { setProperty(it, environment.getProperty(it)) }
                "promena.client.message-broker.consumer.queue.response.error".let { setProperty(it, environment.getProperty(it)) }

                "promena.client.message-broker.spring.activemq.broker-url".let { setProperty(it, environment.getProperty(it)) }
                "promena.client.message-broker.spring.jms.pub-sub-domain".let { setProperty(it, environment.getProperty(it)) }
            }
}