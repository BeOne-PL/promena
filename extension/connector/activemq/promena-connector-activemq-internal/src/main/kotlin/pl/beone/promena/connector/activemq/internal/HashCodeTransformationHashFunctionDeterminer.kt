package pl.beone.promena.connector.activemq.internal

import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer

class HashCodeTransformationHashFunctionDeterminer : TransformationHashFunctionDeterminer {

    override fun determine(transformerIds: List<String>): String =
        transformerIds.distinct()
                .sorted()
                .hashCode()
                .toString()
}