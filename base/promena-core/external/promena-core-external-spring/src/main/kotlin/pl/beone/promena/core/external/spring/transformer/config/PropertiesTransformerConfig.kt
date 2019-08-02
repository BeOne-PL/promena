package pl.beone.promena.core.external.spring.transformer.config

import mu.KotlinLogging
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId
import pl.beone.promena.transformer.contract.transformer.transformerId

class PropertiesTransformerConfig(private val environment: Environment) : TransformerConfig {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getTransformerId(transformer: Transformer): TransformerId =
        transformerId(
            determine(transformer, "id.name", String::class.java, null),
            determine(transformer, "id.sub-name", String::class.java, null)
        )

    override fun getActors(transformer: Transformer): Int =
        determine(transformer, "actors", Int::class.java, 1)

    override fun getPriority(transformer: Transformer): Int =
        determine(transformer, "priority", Int::class.java, 0)

    private fun <T> determine(transformer: Transformer, keyElement: String, clazz: Class<T>, default: T?): T {
        val key = "transformer.${transformer.javaClass.canonicalName}.$keyElement"

        return if (environment.getProperty(key) != null) {
            environment.getRequiredProperty(key, clazz)
        } else {
            if (default == null) {
                throw IllegalStateException("There is no <$key> property")
            } else {
                logger.warn { "There is no <$key> property. Set $keyElement to <$default>" }
                default
            }
        }
    }
}