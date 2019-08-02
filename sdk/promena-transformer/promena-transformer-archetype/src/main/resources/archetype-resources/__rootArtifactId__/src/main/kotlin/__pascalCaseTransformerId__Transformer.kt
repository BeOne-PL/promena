package ${package}

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerCouldNotTransformException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.toTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants

class ${pascalCaseTransformerId}Transformer(private val internalCommunicationParameters: CommunicationParameters) : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
        dataDescriptor.descriptors.map { (data, _, metadata) ->
            singleTransformedDataDescriptor(data, metadata)
        }.toTransformedDataDescriptor()

    override fun canTransform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters) {
        if (dataDescriptor.descriptors.any { it.mediaType != MediaTypeConstants.TEXT_PLAIN } || targetMediaType != MediaTypeConstants.TEXT_PLAIN) {
            throw TransformerCouldNotTransformException("Supported transformations: text/plain -> text/plain")
        }

        try {
            parameters.get(${pascalCaseTransformerId}Constants.Parameters.EXAMPLE)
        } catch (e: NoSuchElementException) {
            throw TransformerCouldNotTransformException("Mandatory parameters: ${${pascalCaseTransformerId}Constants.Parameters.EXAMPLE}")
        }
    }
}