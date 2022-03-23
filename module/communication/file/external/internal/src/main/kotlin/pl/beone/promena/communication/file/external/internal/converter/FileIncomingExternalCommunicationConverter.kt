package pl.beone.promena.communication.file.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.communication.file.model.common.extension.isSubPath
import pl.beone.promena.communication.file.model.common.extension.notIncludedInPath
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.communication.file.model.internal.getDirectory
import pl.beone.promena.communication.file.model.internal.getIsSourceFileVolumeMounted
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationNotFoundException
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

/**
 * Converts a data into [FileData][pl.beone.promena.transformer.internal.model.data.file.FileData]
 * with a file in directory indicated by [internalCommunicationParameters]
 * if [CommunicationParameters.getId] isn't [FileCommunicationParametersConstants.ID].
 * If a data is the type of [FileData][pl.beone.promena.transformer.internal.model.data.file.data.memory.MemoryData], it returns the same instance.
 */
class FileIncomingExternalCommunicationConverter(
    private val internalCommunicationParameters: CommunicationParameters,
    private val internalCommunicationConverter: InternalCommunicationConverter
) : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor {
        if (bothCommunicationsAreFile(externalCommunicationParameters)) {
            val isFileCommunicationCompatible = fileCommunicationsCompatible(externalCommunicationParameters)
            if (!isFileCommunicationCompatible) {
                throw ExternalCommunicationNotFoundException("Incompatible external communication. Please check properties.")
            }
            if (!internalCommunicationParameters.getIsSourceFileVolumeMounted()) {
                logFileCommunicationsPotentialProblems(externalCommunicationParameters)
            }
        }

        logger.warnIfCommunicationsAreDifferent(internalCommunicationParameters.getId(), externalCommunicationParameters.getId())

        return internalCommunicationConverter.convert(dataDescriptor, true)
    }

    private fun bothCommunicationsAreFile(externalCommunicationParameters: CommunicationParameters): Boolean =
        internalCommunicationParameters.getId() == externalCommunicationParameters.getId()

    private fun fileCommunicationsCompatible(externalFileCommunicationParameters: CommunicationParameters): Boolean {
        val id = externalFileCommunicationParameters.getId()
        val internalIsSourceFileVolumeMounted = internalCommunicationParameters.getIsSourceFileVolumeMounted()
        val externalIsSourceFileVolumeMounted = externalFileCommunicationParameters.getIsSourceFileVolumeMounted()
        if (!internalIsSourceFileVolumeMounted && externalIsSourceFileVolumeMounted) {
            logger.error { "Communication <$id>: Client didn't send file data as it expected volume with source files to be mounted to Promena. Please check the properties." }
            return false
        }
        else if (internalIsSourceFileVolumeMounted && !externalIsSourceFileVolumeMounted) {
            logger.error { "Communication <$id>: Client sent file data unnecessarily as volume with source files is mounted to Promena. Transformation will not be executed. Please check the properties." }
            return false
        }
        return true
    }

    private fun logFileCommunicationsPotentialProblems(externalFileCommunicationParameters: CommunicationParameters) {
        val id = externalFileCommunicationParameters.getId()
        val internalCommunicationDirectory = internalCommunicationParameters.getDirectory()
        val externalCommunicationDirectory = externalFileCommunicationParameters.getDirectory()

        when {
            externalCommunicationDirectory.isSubPath(internalCommunicationDirectory) ->
                logger.warn { "Communication <$id>: you should use the same communication directories for performance reasons. Now external communication directory <$externalCommunicationDirectory> is a subpath of internal communication directory <$internalCommunicationDirectory>. It causes one more conversion" }

            externalCommunicationDirectory.notIncludedInPath(internalCommunicationDirectory) ->
                logger.warn { "Communication <$id>: external communication directory <$externalCommunicationDirectory> isn't included in internal communication directory <$internalCommunicationDirectory>. It is highly possible that external communication directory isn't accessible from Promena" }

        }
    }
}
