package pl.beone.promena.connector.activemq.configuration.delivery.jms

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId

internal class TransformationHashCodeMessageSelectorDeterminer {

    companion object {
        private val transformerIdsCombinationDeterminer = TransformerIdsCombinationDeterminer()
    }

    fun determine(transformerConfig: TransformerConfig,
                  transformers: List<Transformer>,
                  transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer): String =
        transformerIdsCombinationDeterminer.determine(getTransformerIds(transformers, transformerConfig))
                .map(transformationHashFunctionDeterminer::determine)
                .let(::createMessageSelector)

    private fun getTransformerIds(transformers: List<Transformer>, transformerConfig: TransformerConfig): List<TransformerId> =
        transformers.map { transformerConfig.getTransformerId(it) }

    private fun createMessageSelector(transformationHashFunctionAllCombinations: List<String>): String =
        PromenaJmsHeaders.TRANSFORMATION_HASH_CODE + " IN (" + transformationHashFunctionAllCombinations.joinToString(", ") { "'$it'" } + ")"
}