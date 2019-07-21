package pl.beone.promena.connector.activemq.integrationtest.test

import io.mockk.every
import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

@Configuration
class TestTransformerMockContext {

    companion object {
        internal const val TRANSFORMER_ID = "test"
    }

    @Bean
    fun testTransformer() =
            mockk<Transformer>()

    @Bean
    fun transformerConfig(testTransformer: Transformer) =
            mockk<TransformerConfig> {
                every { getId(testTransformer) } returns TRANSFORMER_ID
            }
}