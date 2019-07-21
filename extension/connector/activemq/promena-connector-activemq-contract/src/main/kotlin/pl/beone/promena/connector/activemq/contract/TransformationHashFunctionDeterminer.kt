package pl.beone.promena.connector.activemq.contract

interface TransformationHashFunctionDeterminer {

    fun determine(transformerIds: List<String>): String

}