package ${package}

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationWritableDataCreator
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.toTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}Support
import ${package}.processor.Processor

class ${pascalCaseTransformerId}Transformer(
    settings: ${pascalCaseTransformerId}TransformerSettings,
    defaultParameters: ${pascalCaseTransformerId}TransformerDefaultParameters,
    private val communicationParameters: CommunicationParameters,
    private val communicationWritableDataCreator: CommunicationWritableDataCreator
) : Transformer {

    private val processor = Processor(settings, defaultParameters)

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
        dataDescriptor.descriptors
            .map { processor.process(it, parameters, communicationWritableDataCreator.create(communicationParameters)) }
            .toTransformedDataDescriptor()

    override fun isSupported(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters) {
        ${pascalCaseTransformerId}Support.isSupported(dataDescriptor, targetMediaType, parameters)
    }
}