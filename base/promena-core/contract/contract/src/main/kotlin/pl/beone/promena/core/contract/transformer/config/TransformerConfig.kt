package pl.beone.promena.core.contract.transformer.config

import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId

/**
 * Gets information about a transformer.
 */
interface TransformerConfig {

    /**
     * @return the id of [transformer]
     */
    fun getTransformerId(transformer: Transformer): TransformerId

    /**
     * @return the priority of the [transformer] - a lower value indicates a higher priority
     */
    fun getPriority(transformer: Transformer): Int

    /**
     * @return the number of [transformer] actor instances
     */
    fun getActors(transformer: Transformer): Int
}