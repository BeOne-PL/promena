package pl.beone.promena.connector.normal.http.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan("pl.beone.promena.connector.normal.http.delivery")
@PropertySource("classpath:module-connector-normal-http.properties")
class NormalHttpConnectorModuleConfig