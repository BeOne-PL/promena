package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Adjusts data from Promena *internal communication* to *external communication*.
 */
interface OutgoingExternalCommunicationConverter {

    /**
     * Converts [transformedDataDescriptor] into another [TransformedDataDescriptor] considering [externalCommunicationParameters].
     *
     * @return a converted transformed data descriptor
     */
    fun convert(transformedDataDescriptor: TransformedDataDescriptor, externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptor
}