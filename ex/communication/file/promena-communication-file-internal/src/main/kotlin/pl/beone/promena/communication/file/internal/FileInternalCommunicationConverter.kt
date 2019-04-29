package pl.beone.promena.communication.file.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

class FileInternalCommunicationConverter(private val location: URI)
    : InternalCommunicationConverter, AbstractFileCommunicationConverter<TransformedDataDescriptor>() {

    companion object {
        private val logger = LoggerFactory.getLogger(FileInternalCommunicationConverter::class.java)
    }

    override fun logger(): Logger =
            logger

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor =
            convertImpl(transformedDataDescriptor, location, true)

    override fun convertDescriptor(currentDescriptor: TransformedDataDescriptor, data: Data): TransformedDataDescriptor =
            TransformedDataDescriptor(data, currentDescriptor.metadata)

}

