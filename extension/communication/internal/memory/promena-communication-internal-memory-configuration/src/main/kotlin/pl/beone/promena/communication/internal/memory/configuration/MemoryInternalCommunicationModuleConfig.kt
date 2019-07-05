package pl.beone.promena.communication.internal.memory.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-internal-memory.properties")
class MemoryInternalCommunicationModuleConfig