package pl.beone.promena.connector.activemq.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan("pl.beone.promena.connector.activemq.configuration")
@PropertySource("classpath:module-connector-activemq.properties")
class ActiveMQConnectorModuleConfig