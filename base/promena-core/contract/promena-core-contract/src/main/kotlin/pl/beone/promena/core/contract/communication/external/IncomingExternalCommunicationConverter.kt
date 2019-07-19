package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptors

interface IncomingExternalCommunicationConverter {

    fun convert(dataDescriptors: DataDescriptors, externalCommunicationParameters: CommunicationParameters): DataDescriptors
}