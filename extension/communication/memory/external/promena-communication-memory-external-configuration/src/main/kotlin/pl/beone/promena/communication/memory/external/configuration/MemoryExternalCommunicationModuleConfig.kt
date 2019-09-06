package pl.beone.promena.communication.memory.external.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:extension-communication-memory-external.properties")
class MemoryExternalCommunicationModuleConfig