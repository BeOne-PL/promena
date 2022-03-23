@file:JvmName("FileCommunicationParametersDsl")

package pl.beone.promena.communication.file.model.internal

import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import java.io.File

fun fileCommunicationParameters(directory: File, isAlfdataMounted: Boolean) =
    communicationParameters(FileCommunicationParametersConstants.ID) +
            (FileCommunicationParametersConstants.DIRECTORY_KEY to directory) +
            (FileCommunicationParametersConstants.IS_ALFDATA_MOUNTED_KEY to isAlfdataMounted)

fun fileCommunicationParameters(directory: File, alfDataMountDirectory: File,
                                alfDataAlfrescoMountDirectory: File, isAlfdataMounted: Boolean): CommunicationParameters =
    communicationParameters(FileCommunicationParametersConstants.ID) +
            (FileCommunicationParametersConstants.DIRECTORY_KEY to directory) +
            (FileCommunicationParametersConstants.ALFDATA_MOUNT_PATH_KEY to alfDataMountDirectory) +
            (FileCommunicationParametersConstants.ALFDATA_ALFRESCO_MOUNT_PATH_KEY to alfDataAlfrescoMountDirectory) +
            (FileCommunicationParametersConstants.IS_ALFDATA_MOUNTED_KEY to isAlfdataMounted)

fun CommunicationParameters.getDirectory(): File =
    get(FileCommunicationParametersConstants.DIRECTORY_KEY, File::class.java)

fun CommunicationParameters.getAlfdataMountDirectory(): File =
    get(FileCommunicationParametersConstants.ALFDATA_MOUNT_PATH_KEY, File::class.java)

fun CommunicationParameters.getAlfdataAlfrescoMountDirectory(): File =
    get(FileCommunicationParametersConstants.ALFDATA_ALFRESCO_MOUNT_PATH_KEY, File::class.java)

fun CommunicationParameters.getIsAlfdataMounted(): Boolean =
    get(FileCommunicationParametersConstants.IS_ALFDATA_MOUNTED_KEY, Boolean::class.java)