package pl.beone.promena.communication.file.internal

import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.core.common.utils.verifyIfItIsDirectoryAndYouCanCreateFile
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.CommunicationValidator
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class FileCommunicationValidatorConverter : CommunicationValidator {

    override fun validate(dataDescriptors: List<DataDescriptor>, communicationParameters: CommunicationParameters) {
        val communicationLocation = try {
            communicationParameters.getLocation()
        } catch (e: NoSuchElementException) {
            throw CommunicationValidationException("Communication parameters doesn't contain <location>",
                                                                                                                  e)
        }

        try {
            communicationLocation.verifyIfItIsDirectoryAndYouCanCreateFile()
        } catch (e: Exception) {
            throw CommunicationValidationException("Communication location <$communicationLocation> isn't reachable",
                                                                                                                  e)
        }


        dataDescriptors.forEach { it.validate() }
    }

    private fun DataDescriptor.validate() {
        val location = try {
            data.getLocation()
        } catch (e: UnsupportedOperationException) {
            throw CommunicationValidationException("One of data exists only in memory but should be file")
        }

        if (location.scheme != "file") {
            throw CommunicationValidationException("Data location <$location> hasn't <file> scheme")
        }

        try {
            data.isAvailable()
        } catch (e: DataAccessibilityException) {
            throw CommunicationValidationException("Data (<$location>) isn't available",
                                                                                                                  e)
        }
    }

}

