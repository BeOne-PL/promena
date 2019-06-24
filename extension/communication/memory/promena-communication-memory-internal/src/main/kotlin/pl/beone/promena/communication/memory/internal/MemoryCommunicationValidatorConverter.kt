package pl.beone.promena.communication.memory.internal

import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.CommunicationValidator
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class MemoryCommunicationValidatorConverter : CommunicationValidator {

    override fun validate(dataDescriptors: List<DataDescriptor>, communicationParameters: CommunicationParameters) {
        for (dataDescriptor in dataDescriptors) {
            val data = dataDescriptor.data

            try {
                val location = data.getLocation()

                throw CommunicationValidationException("Data has location <$location> but shouldn't")
            } catch (e: UnsupportedOperationException) {
                // it's deliberately. Memory implementation hasn't location
            }

            try {
                data.isAvailable()
            } catch (e: DataAccessibilityException) {
                throw CommunicationValidationException("One of data isn't available", e)
            }
        }
    }

}

