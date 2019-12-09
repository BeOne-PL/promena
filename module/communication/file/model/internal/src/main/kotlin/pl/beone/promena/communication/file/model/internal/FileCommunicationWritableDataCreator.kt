package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationWritableDataCreator
import pl.beone.promena.transformer.contract.model.data.WritableData
import pl.beone.promena.transformer.internal.model.data.file.toFileWritableDataFromDirectory

object FileCommunicationWritableDataCreator : CommunicationWritableDataCreator {

    override fun create(communicationParameters: CommunicationParameters): WritableData =
        communicationParameters.getDirectory()
            .toFileWritableDataFromDirectory()
}