package pl.beone.promena.connector.activemq.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.activemq.internal.HashCodeTransformationHashFunctionDeterminer

@Configuration
class HashCodeTransformationHashFunctionDeterminerContext {

    @Bean
    fun hashCodeTransformationHashFunctionDeterminer() =
        HashCodeTransformationHashFunctionDeterminer()
}