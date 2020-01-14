package pl.beone.promena.transformer.contract.communication

import pl.beone.promena.transformer.contract.model.data.WritableData

interface CommunicationWritableDataCreator {

    /**
     * Creates a native [WritableData] for [communicationParameters].
     */
    fun create(communicationParameters: CommunicationParameters): WritableData
}