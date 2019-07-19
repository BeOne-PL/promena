package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

interface OutgoingExternalCommunicationConverter {

    fun convert(transformedDataDescriptors: TransformedDataDescriptors,
                externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptors
}