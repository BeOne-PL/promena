package pl.beone.promena.connector.activemq.configuration.delivery.jms

import org.apache.commons.math3.util.Combinations

// TODO test it
internal class TransformerIdsCombinationDeterminer {

    fun determine(transformerIds: TransformerIds): List<TransformerIds> {
        val transformerWithId = createIndexAndTransformerIdMap(transformerIds)

        val n = transformerIds.size
        return createRangeForPossibilities(n).flatMap { k ->
            generateTransformerIdsCombinations(n, k, transformerWithId)
        }

    }

    private fun createIndexAndTransformerIdMap(transformerIds: TransformerIds): Map<Int, String> =
        transformerIds.mapIndexed { index, transformerId -> index to transformerId }
                .toMap()

    private fun createRangeForPossibilities(size: Int): IntRange =
        (1..size)

    private fun generateTransformerIdsCombinations(n: Int, k: Int, transformerWithId: Map<Int, String>): List<TransformerIds> =
        Combinations(n, k)
                .map { idCombination ->
                    idCombination.toList().map { transformerWithId.getValue(it) }
                }
}