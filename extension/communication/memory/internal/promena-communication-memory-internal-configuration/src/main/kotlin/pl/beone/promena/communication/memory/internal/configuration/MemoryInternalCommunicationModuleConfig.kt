package pl.beone.promena.communication.memory.internal.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-memory-internal.properties")
class MemoryInternalCommunicationModuleConfig