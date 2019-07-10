package ${package}

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class ${transformerClassName}(private val internalCommunicationParameters: CommunicationParameters) : Transformer {

    companion object {
        private val memoryDataProcessor = MemoryDataProcessor()
        private val fileDataProcessor = FileDataProcessor()
    }

    override fun transform(dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> =
            dataDescriptors.map { process(it.data, internalCommunicationParameters.getId()) }
                    .map { TransformedDataDescriptor(it, MapMetadata.empty()) }

    private fun process(data: Data, communicationId: String): Data =
            when (communicationId) {
                "file" -> fileDataProcessor.transform(data)
                else   -> memoryDataProcessor.transform(data) // back pressure
            }

    override fun canTransform(dataDescriptors: List<DataDescriptor>,
                              targetMediaType: MediaType,
                              parameters: Parameters): Boolean =
            dataDescriptors.allTextPlain() && targetMediaType.isTextPlain()

    private fun List<DataDescriptor>.allTextPlain(): Boolean =
            all { it.mediaType.isTextPlain() }

    private fun MediaType.isTextPlain(): Boolean =
            this == TEXT_PLAIN

}