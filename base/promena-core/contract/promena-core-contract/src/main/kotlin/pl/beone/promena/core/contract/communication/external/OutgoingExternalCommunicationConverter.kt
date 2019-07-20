package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

interface OutgoingExternalCommunicationConverter {

    fun convert(transformedDataDescriptor: TransformedDataDescriptor,
                externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptor
}