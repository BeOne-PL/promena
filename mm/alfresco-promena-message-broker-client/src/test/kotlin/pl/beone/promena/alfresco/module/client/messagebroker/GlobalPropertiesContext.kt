package pl.beone.promena.alfresco.module.client.messagebroker

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.testcontainers.containers.GenericContainer
import java.util.*

@Configuration
class GlobalPropertiesContext(private val environment: Environment,
                              private val activeMQContainer: GenericContainer<Nothing>) {

    @Bean("global-properties")
    fun globalProperties(): Properties =
            Properties().apply {
                "promena.transformation.error.tryAgain".let { setProperty(it, environment.getProperty(it)) }
                "promena.transformation.error.delay".let { setProperty(it, environment.getProperty(it)) }

                "promena.client.message-broker.waitMax".let { setProperty(it, environment.getProperty(it)) }

                "promena.client.message-broker.consumer.queue.request".let { setProperty(it, environment.getProperty(it)) }
                "promena.client.message-broker.consumer.queue.response".let { setProperty(it, environment.getProperty(it)) }
                "promena.client.message-broker.consumer.queue.response.error".let { setProperty(it, environment.getProperty(it)) }

                "promena.client.message-broker.spring.activemq.broker-url".let {
                    setProperty(it, environment.getProperty(it)?.replace("{PORT}", activeMQContainer.getMappedPort(61616).toString()))
                }
                "promena.client.message-broker.spring.jms.pub-sub-domain".let { setProperty(it, environment.getProperty(it)) }
            }
}