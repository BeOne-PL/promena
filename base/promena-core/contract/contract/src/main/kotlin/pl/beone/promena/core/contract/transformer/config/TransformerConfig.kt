package pl.beone.promena.core.contract.transformer.config

import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId

interface TransformerConfig {

    fun getTransformerId(transformer: Transformer): TransformerId

    fun getPriority(transformer: Transformer): Int

    fun getActors(transformer: Transformer): Int
}