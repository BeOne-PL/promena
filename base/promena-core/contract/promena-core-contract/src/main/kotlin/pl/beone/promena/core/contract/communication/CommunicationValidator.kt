package pl.beone.promena.core.contract.communication

import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

interface CommunicationValidator {

    @Throws(CommunicationValidationException::class)
    fun validate(dataDescriptors: List<DataDescriptor>, communicationParameters: CommunicationParameters)
}