package pl.beone.promena.communication.file.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.communication.file.model.common.extension.isSubPath
import pl.beone.promena.communication.file.model.common.extension.notIncludedInPath
import pl.beone.promena.communication.file.model.internal.getDirectory
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

class FileIncomingExternalCommunicationConverter(
    private val internalCommunicationParameters: CommunicationParameters,
    private val internalCommunicationConverter: InternalCommunicationConverter
) : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor {
        if (bothCommunicationsAreFile(externalCommunicationParameters)) {
            logFileCommunicationsPotentialProblems(externalCommunicationParameters)
        }

        logger.warnIfCommunicationsAreDifferent(internalCommunicationParameters.getId(), externalCommunicationParameters.getId())

        return internalCommunicationConverter.convert(dataDescriptor)
    }

    private fun bothCommunicationsAreFile(externalCommunicationParameters: CommunicationParameters): Boolean =
        internalCommunicationParameters.getId() == externalCommunicationParameters.getId()

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
