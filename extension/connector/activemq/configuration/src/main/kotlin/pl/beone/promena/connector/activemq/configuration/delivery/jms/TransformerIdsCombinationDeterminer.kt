package pl.beone.promena.connector.activemq.configuration.delivery.jms

import org.apache.commons.math3.util.Combinations
import pl.beone.promena.transformer.contract.transformer.TransformerId
import pl.beone.promena.transformer.contract.transformer.toTransformerId

internal object TransformerIdsCombinationDeterminer {

    fun determine(transformerIds: List<TransformerId>): List<List<TransformerId>> {
        val generalTransformerIds = createGeneralTransformerIds(transformerIds)
        val allTransformersIds = (generalTransformerIds + transformerIds).distinct()

        val idToTransformerMap = createIndexToTransformerIdMap(allTransformersIds)

        val n = allTransformersIds.size
        return createRangeForPossibilities(n).flatMap { k ->
            generateTransformerIdsCombinations(n, k, idToTransformerMap)
        }

    }

    private fun createGeneralTransformerIds(transformerIds: List<TransformerId>): List<TransformerId> =
        transformerIds.map { it.name }
            .distinct()
            .map(String::toTransformerId)

    private fun createIndexToTransformerIdMap(transformerIds: List<TransformerId>): Map<Int, TransformerId> =
        transformerIds.mapIndexed { index, transformerId -> index to transformerId }
            .toMap()

    private fun createRangeForPossibilities(size: Int): IntRange =
        (1..size)

    private fun generateTransformerIdsCombinations(n: Int, k: Int, idToTransformerMap: Map<Int, TransformerId>): List<List<TransformerId>> =
        Combinations(n, k)
            .map { idCombination -> idCombination.toList().map { idToTransformerMap.getValue(it) } }
}