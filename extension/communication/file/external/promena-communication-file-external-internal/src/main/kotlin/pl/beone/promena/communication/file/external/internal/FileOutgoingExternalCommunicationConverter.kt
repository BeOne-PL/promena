package pl.beone.promena.communication.file.external.internal

import pl.beone.promena.communication.file.utils.FileDescriptorConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import java.net.URI

class FileOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    override fun convert(
        transformedDataDescriptor: TransformedDataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor =
        FileDescriptorConverter(externalCommunicationParameters.getCommunicationLocation())
            .convert(transformedDataDescriptor)

    private fun CommunicationParameters.getCommunicationLocation(): URI =
        get("location", URI::class.java)
}
