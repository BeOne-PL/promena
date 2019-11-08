package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

data class DefaultFileCommunicationParameters internal constructor(
    private val communicationParameters: CommunicationParameters
) :
    FileCommunicationParameters, CommunicationParameters by communicationParameters {

    companion object {
        const val DIRECTORY = "directory"
    }

    override fun getDirectory(): File =
        get(DIRECTORY, File::class.java)
}