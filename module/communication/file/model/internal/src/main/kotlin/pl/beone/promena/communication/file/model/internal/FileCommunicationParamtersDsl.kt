@file:JvmName("FileCommunicationParametersDsl")

package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import java.io.File

fun fileCommunicationParameters(directory: File, isSourceFileVolumeMounted: Boolean) =
    communicationParameters(FileCommunicationParametersConstants.ID) +
            (FileCommunicationParametersConstants.DIRECTORY_KEY to directory) +
            (FileCommunicationParametersConstants.IS_SOURCE_FILE_VOLUME_MOUNTED_KEY to isSourceFileVolumeMounted)

fun fileCommunicationParameters(directory: File, sourceFileVolumeMountDirectory: File,
                                sourceFileVolumeExternalMountDirectory: File, isSourceFileVolumeMounted: Boolean): CommunicationParameters =
    communicationParameters(FileCommunicationParametersConstants.ID) +
            (FileCommunicationParametersConstants.DIRECTORY_KEY to directory) +
            (FileCommunicationParametersConstants.SOURCE_FILE_VOLUME_MOUNT_PATH_KEY to sourceFileVolumeMountDirectory) +
            (FileCommunicationParametersConstants.SOURCE_FILE_VOLUME_EXTERNAL_MOUNT_PATH_KEY to sourceFileVolumeExternalMountDirectory) +
            (FileCommunicationParametersConstants.IS_SOURCE_FILE_VOLUME_MOUNTED_KEY to isSourceFileVolumeMounted)

fun CommunicationParameters.getDirectory(): File =
    get(FileCommunicationParametersConstants.DIRECTORY_KEY, File::class.java)

fun CommunicationParameters.getSourceFileVolumeMountDirectory(): File =
    get(FileCommunicationParametersConstants.SOURCE_FILE_VOLUME_MOUNT_PATH_KEY, File::class.java)

fun CommunicationParameters.getSourceFileVolumeExternalMountDirectory(): File =
    get(FileCommunicationParametersConstants.SOURCE_FILE_VOLUME_EXTERNAL_MOUNT_PATH_KEY, File::class.java)

fun CommunicationParameters.getIsSourceFileVolumeMounted(): Boolean =
    get(FileCommunicationParametersConstants.IS_SOURCE_FILE_VOLUME_MOUNTED_KEY, Boolean::class.java)