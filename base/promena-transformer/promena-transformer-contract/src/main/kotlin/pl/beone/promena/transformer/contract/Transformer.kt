package pl.beone.promena.transformer.contract

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

interface Transformer {

    @Throws(TimeoutException::class)
    fun transform(dataDescriptors: List<DataDescriptor>,
                  targetMediaType: MediaType,
                  parameters: Parameters): List<TransformedDataDescriptor>

    fun canTransform(dataDescriptors: List<DataDescriptor>,
                     targetMediaType: MediaType,
                     parameters: Parameters): Boolean
}