package pl.beone.promena.core.contract.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

interface TransformerService {

    fun transform(transformerId: String,
                  dataDescriptors: List<DataDescriptor>,
                  targetMediaType: MediaType,
                  parameters: Parameters): List<TransformedDataDescriptor>

}