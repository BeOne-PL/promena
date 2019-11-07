package pl.beone.promena.transformer.contract.communication

import pl.beone.promena.transformer.contract.model.data.WritableData

interface CommunicationWritableDataCreator {

    fun create(communicationParameters: CommunicationParameters): WritableData
}