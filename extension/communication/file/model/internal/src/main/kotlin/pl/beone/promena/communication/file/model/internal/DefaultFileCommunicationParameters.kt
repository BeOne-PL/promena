package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

class DefaultFileCommunicationParameters internal constructor(private val communicationParameters: CommunicationParameters) :
    FileCommunicationParameters, CommunicationParameters by communicationParameters {

    companion object {
        const val INTERNAL_DIRECTORY = "directory"
        const val EXTERNAL_DIRECTORY = "directoryPath"
    }

    override fun getDirectory(): File =
        try {
            File(get(EXTERNAL_DIRECTORY, String::class.java))
        } catch (e: NoSuchElementException) {
            try {
                get(INTERNAL_DIRECTORY, File::class.java)
            } catch (e: NoSuchElementException) {
                throw NoSuchElementException("There is neither <$INTERNAL_DIRECTORY> (internal) nor <$EXTERNAL_DIRECTORY> (external) elements")
            }
        }
}