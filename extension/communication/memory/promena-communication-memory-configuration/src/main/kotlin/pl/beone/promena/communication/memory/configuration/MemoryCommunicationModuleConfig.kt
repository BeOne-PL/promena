package pl.beone.promena.communication.memory.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-communication-memory.properties")
class MemoryCommunicationModuleConfig