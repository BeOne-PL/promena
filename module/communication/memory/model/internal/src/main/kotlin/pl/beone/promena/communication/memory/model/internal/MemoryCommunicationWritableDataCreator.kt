package pl.beone.promena.communication.memory.model.internal

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationWritableDataCreator
import pl.beone.promena.transformer.contract.model.data.WritableData
import pl.beone.promena.transformer.internal.model.data.memory.emptyMemoryWritableData

object MemoryCommunicationWritableDataCreator : CommunicationWritableDataCreator {

    override fun create(communicationParameters: CommunicationParameters): WritableData =
        emptyMemoryWritableData()
}