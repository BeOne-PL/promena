package pl.beone.promena.communication.file.external.internal.converter

import pl.beone.promena.communication.file.model.common.converter.FileDescriptorConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.communication.file.model.internal.getDirectory
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Converts a data into [FileData][pl.beone.promena.transformer.internal.model.data.file.FileData]
 * with a file in directory indicated by [externalCommunicationParameters]
 * if [CommunicationParameters.getId] isn't [FileCommunicationParametersConstants.ID].
 * If a data is the type of [FileData][pl.beone.promena.transformer.internal.model.data.file.data.memory.MemoryData], it returns the same instance.
 */
object FileOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    override fun convert(
        transformedDataDescriptor: TransformedDataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor =
        FileDescriptorConverter(externalCommunicationParameters.getDirectory())
            .convert(transformedDataDescriptor, false)
}

