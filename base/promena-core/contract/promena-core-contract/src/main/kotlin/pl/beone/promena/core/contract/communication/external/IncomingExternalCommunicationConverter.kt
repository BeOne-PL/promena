package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

interface IncomingExternalCommunicationConverter {

    @Throws(CommunicationParametersValidationException::class)
    fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor
}