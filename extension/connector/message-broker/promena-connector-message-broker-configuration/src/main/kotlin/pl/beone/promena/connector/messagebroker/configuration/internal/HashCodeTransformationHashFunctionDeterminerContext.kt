package pl.beone.promena.connector.messagebroker.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.messagebroker.internal.HashCodeTransformationHashFunctionDeterminer

@Configuration
class HashCodeTransformationHashFunctionDeterminerContext {

    @Bean
    fun hashCodeTransformationHashFunctionDeterminer() =
        HashCodeTransformationHashFunctionDeterminer()
}