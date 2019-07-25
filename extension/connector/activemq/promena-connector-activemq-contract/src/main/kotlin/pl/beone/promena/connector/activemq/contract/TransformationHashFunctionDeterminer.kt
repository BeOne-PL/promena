package pl.beone.promena.connector.activemq.contract

import pl.beone.promena.transformer.contract.transformer.TransformerId

interface TransformationHashFunctionDeterminer {

    fun determine(transformerIds: List<TransformerId>): String

}