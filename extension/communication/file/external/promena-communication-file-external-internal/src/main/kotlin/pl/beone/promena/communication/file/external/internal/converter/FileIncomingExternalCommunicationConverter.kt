package pl.beone.promena.communication.file.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.communication.file.common.extension.getExternalCommunicationDirectory
import pl.beone.promena.communication.file.common.extension.getInternalCommunicationDirectory
import pl.beone.promena.communication.file.common.extension.isSubPath
import pl.beone.promena.communication.file.common.extension.notIncludedInPath
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import java.io.File

class FileIncomingExternalCommunicationConverter(
    private val externalCommunicationId: String,
    private val internalCommunicationId: String,
    private val internalCommunicationParameters: CommunicationParameters,
    private val internalCommunicationConverter: InternalCommunicationConverter
) : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor {
        val externalCommunicationDirectory = validateAndGetExternalCommunicationDirectory(externalCommunicationParameters)

        if (bothCommunicationsAreFile()) {
            logFileCommunicationsPotentialProblems(externalCommunicationDirectory)
        }

        logger.warnIfCommunicationsAreDifferent(internalCommunicationId, externalCommunicationId)

        return internalCommunicationConverter.convert(dataDescriptor)
    }

    private fun validateAndGetExternalCommunicationDirectory(externalCommunicationParameters: CommunicationParameters): File =
        try {
            externalCommunicationParameters.getExternalCommunicationDirectory()
        } catch (e: NoSuchElementException) {
            throw CommunicationParametersValidationException("Communication <$externalCommunicationId>: parameter <directoryPath> is mandatory")
        }

    private fun bothCommunicationsAreFile(): Boolean =
        externalCommunicationId == internalCommunicationId

    private fun logFileCommunicationsPotentialProblems(externalCommunicationDirectory: File) {
        val internalCommunicationDirectory = internalCommunicationParameters.getInternalCommunicationDirectory()

        when {
            externalCommunicationDirectory.isSubPath(internalCommunicationDirectory) ->
                logger.warn { "Communication <$externalCommunicationId>: you should use the same communication directories for performance reasons. Now external communication directory <$externalCommunicationDirectory> is a subpath of internal communication directory <$internalCommunicationDirectory>. It causes one more conversion" }

            externalCommunicationDirectory.notIncludedInPath(internalCommunicationDirectory) ->
                logger.warn { "Communication <$externalCommunicationId>: external communication directory <$externalCommunicationDirectory> isn't included in internal communication directory <$internalCommunicationDirectory>. It is highly possible that external communication directory isn't accessible from Promena" }

        }
    }
}
