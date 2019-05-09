package pl.beone.promena.connector.http.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("module-connector-http.properties")
class HttpConnectorModuleConfig