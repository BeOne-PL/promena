package pl.beone.promena.communication.file.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.communication.file.common.extension.getLocation
import pl.beone.promena.communication.file.common.extension.isSubPath
import pl.beone.promena.communication.file.common.extension.notIncludedIn
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import java.net.URI

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
        val externalCommunicationLocation = validateAndGetExternalCommunicationLocation(externalCommunicationParameters)

        if (bothCommunicationsAreFile()) {
            logFileCommunicationsPotentialProblems(externalCommunicationLocation)
        }

        logger.warnIfCommunicationsAreDifferent(internalCommunicationId, externalCommunicationId)

        return internalCommunicationConverter.convert(dataDescriptor)
    }

    private fun validateAndGetExternalCommunicationLocation(externalCommunicationParameters: CommunicationParameters): URI =
        try {
            externalCommunicationParameters.getLocation()
        } catch (e: NoSuchElementException) {
            throw CommunicationParametersValidationException("Communication <$externalCommunicationId>: parameter <location> is mandatory")
        }

    private fun bothCommunicationsAreFile(): Boolean =
        externalCommunicationId == internalCommunicationId

    private fun logFileCommunicationsPotentialProblems(externalCommunicationLocation: URI) {
        val internalCommunicationLocation = internalCommunicationParameters.getLocation()

        when {
            externalCommunicationLocation.isSubPath(internalCommunicationLocation) ->
                logger.warn { "Communication <$externalCommunicationId>: you should use the same communication locations for performance reasons. Now external communication location <$externalCommunicationLocation> is a subpath of internal communication location <$internalCommunicationLocation>. It causes one more conversion" }

            externalCommunicationLocation.notIncludedIn(internalCommunicationLocation) ->
                logger.warn { "Communication <$externalCommunicationId>: external communication location <$externalCommunicationLocation> isn't included in internal communication location <$internalCommunicationLocation>. It is highly possible that external communication location isn't accessible from Promena" }

        }
    }
}
