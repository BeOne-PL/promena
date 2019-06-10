package pl.beone.promena.communication.file.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-file.properties")
class FileCommunicationModuleConfig