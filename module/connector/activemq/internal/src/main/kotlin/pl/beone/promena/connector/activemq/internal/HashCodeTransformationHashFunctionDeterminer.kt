package pl.beone.promena.connector.activemq.internal

import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.transformer.contract.transformer.TransformerId

/**
 * Generates a hash code of sorted and unique transformer ids.
 */
object HashCodeTransformationHashFunctionDeterminer : TransformationHashFunctionDeterminer {

    override fun determine(transformerIds: List<TransformerId>): String =
        transformerIds
            .distinct()
            .map { it.name + it.subName }
            .sorted()
            .hashCode()
            .toString()
}