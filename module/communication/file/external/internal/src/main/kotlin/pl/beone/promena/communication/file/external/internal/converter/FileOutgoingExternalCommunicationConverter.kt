package pl.beone.promena.communication.file.external.internal.converter

import pl.beone.promena.communication.file.model.common.converter.FileDescriptorConverter
import pl.beone.promena.communication.file.model.internal.getDirectory
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

object FileOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    override fun convert(
        transformedDataDescriptor: TransformedDataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor =
        FileDescriptorConverter(externalCommunicationParameters.getDirectory())
            .convert(transformedDataDescriptor)
}

