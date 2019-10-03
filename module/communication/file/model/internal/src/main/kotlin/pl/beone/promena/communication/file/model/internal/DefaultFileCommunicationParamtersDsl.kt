@file:JvmName("DefaultFileCommunicationParametersDsl")

package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import java.io.File

fun fileCommunicationParameters(directory: File): DefaultFileCommunicationParameters =
    DefaultFileCommunicationParameters(
        communicationParameters(FileCommunicationParameters.ID) + (DefaultFileCommunicationParameters.DIRECTORY to directory)
    )