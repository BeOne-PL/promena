@file:JvmName("FileCommunicationParametersDsl")

package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import java.io.File

fun fileCommunicationParameters(directory: File): CommunicationParameters =
    communicationParameters(FileCommunicationParametersConstants.ID) +
            (FileCommunicationParametersConstants.DIRECTORY_KEY to directory)

fun CommunicationParameters.getDirectory(): File =
    get(FileCommunicationParametersConstants.DIRECTORY_KEY, File::class.java)