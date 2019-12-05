package pl.beone.promena.alfresco.module.connector.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.testcontainers.containers.GenericContainer
import java.util.*

@Configuration
class GlobalPropertiesContext(
    private val environment: Environment,
    private val activeMQContainer: GenericContainer<Nothing>
) {

    @Bean("global-properties")
    fun globalProperties(): Properties =
        Properties().apply {
            "promena.core.serializer.kryo.buffer-size".let { setProperty(it, environment.getProperty(it)) }

            "promena.core.communication.external.id".let { setProperty(it, environment.getProperty(it)) }
            "promena.core.communication.external.file.directory.path".let { setProperty(it, environment.getProperty(it)) }

            "promena.core.transformation.error.retry.enabled".let { setProperty(it, environment.getProperty(it)) }
            "promena.core.transformation.error.retry.max-attempts".let { setProperty(it, environment.getProperty(it)) }
            "promena.core.transformation.error.retry.next-attempt-delay".let { setProperty(it, environment.getProperty(it)) }

            "promena.core.transformation.manager.persist-in-alfresco".let { setProperty(it, environment.getProperty(it)) }
            "promena.core.transformation.manager.buffer-size".let { setProperty(it, environment.getProperty(it)) }
            "promena.core.transformation.manager.wait-max".let { setProperty(it, environment.getProperty(it)) }

            "promena.connector.activemq.consumer.queue.request".let { setProperty(it, environment.getProperty(it)) }
            "promena.connector.activemq.consumer.queue.response".let { setProperty(it, environment.getProperty(it)) }
            "promena.connector.activemq.consumer.queue.response.error".let { setProperty(it, environment.getProperty(it)) }
            "promena.connector.activemq.consumer.queue.response.error.selector".let { setProperty(it, environment.getProperty(it)) }

            "promena.connector.activemq.spring.activemq.broker-url".let {
                setProperty(it, environment.getProperty(it)?.replace("{PORT}", activeMQContainer.getMappedPort(61616).toString()))
            }
            "promena.connector.activemq.spring.jms.pub-sub-domain".let { setProperty(it, environment.getProperty(it)) }
        }
}