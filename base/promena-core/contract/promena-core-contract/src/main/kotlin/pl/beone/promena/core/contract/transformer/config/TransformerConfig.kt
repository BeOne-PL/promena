package pl.beone.promena.core.contract.transformer.config

import pl.beone.promena.transformer.contract.Transformer

interface TransformerConfig {

    fun getId(transformer: Transformer): String

    fun getPriority(transformer: Transformer): Int

    fun getActors(transformer: Transformer): Int
}