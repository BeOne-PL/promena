package pl.beone.promena.communication.file.internal.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-file-internal.properties")
class FileInternalCommunicationModuleConfig