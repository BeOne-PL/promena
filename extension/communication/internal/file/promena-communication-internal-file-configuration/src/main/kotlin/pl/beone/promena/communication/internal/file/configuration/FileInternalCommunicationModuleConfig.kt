package pl.beone.promena.communication.internal.file.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-internal-file.properties")
class FileInternalCommunicationModuleConfig