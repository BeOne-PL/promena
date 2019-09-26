package pl.beone.promena.connector.activemq.configuration.delivery.jms

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_HASH_CODE
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId

internal object TransformationHashCodeMessageSelectorDeterminer {

    fun determine(
        transformerConfig: TransformerConfig,
        transformers: List<Transformer>,
        transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer
    ): String =
        TransformerIdsCombinationDeterminer.determine(getTransformerIds(transformers, transformerConfig))
            .map(transformationHashFunctionDeterminer::determine)
            .let(::createMessageSelector)

    private fun getTransformerIds(transformers: List<Transformer>, transformerConfig: TransformerConfig): List<TransformerId> =
        transformers.map(transformerConfig::getTransformerId)

    private fun createMessageSelector(transformationHashFunctionAllCombinations: List<String>): String =
        TRANSFORMATION_HASH_CODE + " IN (" + transformationHashFunctionAllCombinations.joinToString(", ") { "'$it'" } + ")"
}