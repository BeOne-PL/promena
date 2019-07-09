package pl.beone.promena.communication.external.file.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-external-file.properties")
class FileExternalCommunicationModuleConfig