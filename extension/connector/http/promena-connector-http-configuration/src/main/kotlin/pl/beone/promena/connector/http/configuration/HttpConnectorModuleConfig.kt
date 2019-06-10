package pl.beone.promena.connector.http.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan("pl.beone.promena.connector.http.configuration")
@PropertySource("classpath:module-connector-http.properties")
class HttpConnectorModuleConfig