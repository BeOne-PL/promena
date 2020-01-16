package pl.beone.promena.connector.activemq.contract

import pl.beone.promena.transformer.contract.transformer.TransformerId

interface TransformationHashFunctionDeterminer {

    /**
     * @return the value of a hash function generated from [transformerIds]
     */
    fun determine(transformerIds: List<TransformerId>): String
}