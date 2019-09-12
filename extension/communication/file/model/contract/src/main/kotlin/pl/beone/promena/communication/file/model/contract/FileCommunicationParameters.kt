package pl.beone.promena.communication.file.model.contract

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

interface FileCommunicationParameters : CommunicationParameters {

    companion object {
        const val ID = "file"
    }

    fun getDirectory(): File
}