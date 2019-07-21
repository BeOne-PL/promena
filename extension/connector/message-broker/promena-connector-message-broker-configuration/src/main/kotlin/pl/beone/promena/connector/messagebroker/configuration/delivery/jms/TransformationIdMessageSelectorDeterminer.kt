package pl.beone.promena.connector.messagebroker.configuration.delivery.jms

import pl.beone.promena.connector.messagebroker.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.messagebroker.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

// TODO test it
internal class TransformationIdMessageSelectorDeterminer {

    companion object {
        private val transformerIdsCombinationDeterminer = TransformerIdsCombinationDeterminer()
    }

    fun determine(transformerConfig: TransformerConfig,
                  transformers: List<Transformer>,
                  transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer): String =
        transformerIdsCombinationDeterminer.determine(getTransformerIds(transformers, transformerConfig))
                .map(transformationHashFunctionDeterminer::determine)
                .let(this::createMessageSelector)

    private fun getTransformerIds(transformers: List<Transformer>, transformerConfig: TransformerConfig): TransformerIds =
        transformers.map { transformerConfig.getId(it) }

    private fun createMessageSelector(transformationHashFunctionAllCombinations: List<String>): String =
        PromenaJmsHeaders.TRANSFORMATION_ID + " IN (" + transformationHashFunctionAllCombinations.joinToString(", ") { "'$it'" } + ")"
}