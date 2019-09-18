package ${package}

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.toTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.EXAMPLE

class ${pascalCaseTransformerId}Transformer(
    private val internalCommunicationParameters: CommunicationParameters
) : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
        dataDescriptor.descriptors
            .map { (data, _, metadata) -> singleTransformedDataDescriptor(data, metadata) }
            .toTransformedDataDescriptor()

    override fun isSupported(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters) {
        if (dataDescriptor.descriptors.any { it.mediaType.mimeType != TEXT_PLAIN.mimeType } || targetMediaType.mimeType != TEXT_PLAIN.mimeType) {
            throw TransformationNotSupportedException("Supported transformation: text/plain -> text/plain")
        }

        try {
            parameters.get(EXAMPLE)
        } catch (e: NoSuchElementException) {
            throw TransformationNotSupportedException("Mandatory parameter: $EXAMPLE")
        }
    }
}