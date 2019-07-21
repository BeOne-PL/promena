package pl.beone.promena.connector.messagebroker.integrationtest.test

import io.mockk.every
import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

@Configuration
class MockContext {

    companion object {
        internal const val transformerId = "test"
    }

    @Bean
    fun testTransformer() =
            mockk<Transformer>()

    @Bean
    fun transformerConfig(testTransformer: Transformer) =
            mockk<TransformerConfig> {
                every { getId(testTransformer) } returns transformerId
            }
}