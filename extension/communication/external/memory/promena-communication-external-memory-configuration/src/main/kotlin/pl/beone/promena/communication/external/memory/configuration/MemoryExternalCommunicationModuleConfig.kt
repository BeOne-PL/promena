package pl.beone.promena.communication.external.memory.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-external-memory.properties")
class MemoryExternalCommunicationModuleConfig