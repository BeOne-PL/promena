package pl.beone.promena.connector.messagebroker.internal

import pl.beone.promena.connector.messagebroker.contract.TransformationHashFunctionDeterminer

class HashCodeTransformationHashFunctionDeterminer : TransformationHashFunctionDeterminer {

    override fun determine(transformerIds: List<String>): String =
        transformerIds.distinct()
                .sorted()
                .hashCode()
                .toString()
}