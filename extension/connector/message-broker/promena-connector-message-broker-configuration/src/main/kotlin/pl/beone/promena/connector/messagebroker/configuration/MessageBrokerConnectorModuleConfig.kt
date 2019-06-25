package pl.beone.promena.connector.messagebroker.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-connector-message-broker.properties")
class MessageBrokerConnectorModuleConfig