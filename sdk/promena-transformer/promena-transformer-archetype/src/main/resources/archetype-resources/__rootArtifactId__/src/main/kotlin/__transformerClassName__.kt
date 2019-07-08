package ${package}

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.io.File

class ${transformerClassName}(private val internalCommunicationParameters: CommunicationParameters) : Transformer {

    override fun transform(dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> =
            dataDescriptors.map { transformData(it.data, internalCommunicationParameters.getId()) }
                    .map { TransformedDataDescriptor(it, MapMetadata.empty()) }

    private fun transformData(data: Data, communicationId: String): Data =
            when (communicationId) {
                "file" -> data.getFile()
                        .addHashAtTheEnd()
                        .toFileData()

                else   -> data.convertToString()
                        .addHashAtTheEnd()
                        .toInMemoryData()
            }


    private fun Data.getFile(): File =
            File(this.getLocation())

    private fun File.addHashAtTheEnd(): File =
            apply {
                appendText("#")
            }

    private fun File.toFileData(): FileData =
            FileData(toURI())

    private fun Data.convertToString(): String =
            String(this.getBytes())

    private fun String.addHashAtTheEnd(): String =
            "$this#"

    private fun String.toInMemoryData(): InMemoryData =
            InMemoryData(toByteArray())

    override fun canTransform(dataDescriptors: List<DataDescriptor>,
                              targetMediaType: MediaType,
                              parameters: Parameters): Boolean =
            targetMediaType == TEXT_PLAIN

}