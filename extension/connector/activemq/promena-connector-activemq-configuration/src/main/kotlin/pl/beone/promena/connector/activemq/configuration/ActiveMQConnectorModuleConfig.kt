package pl.beone.promena.connector.activemq.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-connector-activemq.properties")
class ActiveMQConnectorModuleConfig