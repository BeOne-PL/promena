package ${package}

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class ${transformerClassName} : Transformer {

    override fun transform(dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> =
            dataDescriptors.map { TransformedDataDescriptor(it.data, MapMetadata.empty()) }

    override fun canTransform(dataDescriptors: List<DataDescriptor>,
                              targetMediaType: MediaType,
                              parameters: Parameters): Boolean =
            targetMediaType == MediaTypeConstants.TEXT_PLAIN
}