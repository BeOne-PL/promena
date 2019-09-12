package pl.beone.promena.communication.file.external.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:extension-communication-file-external.properties")
class FileExternalCommunicationModuleConfig