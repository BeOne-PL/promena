package pl.beone.promena.core.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan(basePackages = [
    "pl.beone.promena"
])
@PropertySource("application-ts.properties",
                "application-akka.properties",
                "application-kryo-classes.properties")
class ApplicationContext