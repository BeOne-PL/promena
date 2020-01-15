package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

/**
 * Adjusts data received from outside (*external communication*) to Promena *internal communication*.
 */
interface IncomingExternalCommunicationConverter {

    /**
     * Converts [dataDescriptor] into another [DataDescriptor] considering [externalCommunicationParameters].
     *
     * @return a converted data descriptor
     */
    fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor
}