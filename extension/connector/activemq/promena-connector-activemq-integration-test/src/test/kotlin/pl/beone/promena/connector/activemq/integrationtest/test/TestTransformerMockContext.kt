package pl.beone.promena.connector.activemq.integrationtest.test

import io.mockk.every
import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.toTransformerId

@Configuration
class TestTransformerMockContext {

    companion object {
        internal val TRANSFORMER_ID = ("test" to "sub").toTransformerId()
    }

    @Bean
    fun testTransformer() =
        mockk<Transformer>()

    @Bean
    fun transformerConfig(testTransformer: Transformer) =
        mockk<TransformerConfig> {
            every { getTransformerId(testTransformer) } returns TRANSFORMER_ID
        }
}