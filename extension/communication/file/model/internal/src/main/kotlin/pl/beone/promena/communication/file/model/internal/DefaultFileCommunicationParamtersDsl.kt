@file:JvmName("DefaultFileCommunicationParametersDsl")

package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import java.io.File

fun internalFileCommunicationParameters(directory: File): DefaultFileCommunicationParameters =
    DefaultFileCommunicationParameters(
        communicationParameters(FileCommunicationParameters.ID) + (DefaultFileCommunicationParameters.INTERNAL_DIRECTORY to directory)
    )

fun externalFileCommunicationParameters(directory: File): DefaultFileCommunicationParameters =
    DefaultFileCommunicationParameters(
        communicationParameters(FileCommunicationParameters.ID) + (DefaultFileCommunicationParameters.EXTERNAL_DIRECTORY to directory.path)
    )