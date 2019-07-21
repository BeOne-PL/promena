package pl.beone.promena.connector.messagebroker.contract

interface TransformationHashFunctionDeterminer {

    fun determine(transformerIds: List<String>): String

}