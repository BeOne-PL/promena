package pl.beone.promena.lib.http.client.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

interface TransformationServerService {

    fun transform(transformerId: String,
                  dataDescriptors: List<DataDescriptor>,
                  targetMediaType: MediaType,
                  parameters: Parameters,
                  timeout: Long?): List<TransformedDataDescriptor>
}