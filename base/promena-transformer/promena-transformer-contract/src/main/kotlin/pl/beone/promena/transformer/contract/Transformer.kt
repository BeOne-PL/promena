package pl.beone.promena.transformer.contract

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

interface Transformer {

    @Throws(TimeoutException::class)
    fun transform(dataDescriptors: DataDescriptors,
                  targetMediaType: MediaType,
                  parameters: Parameters): TransformedDataDescriptors

    fun canTransform(dataDescriptors: DataDescriptors,
                     targetMediaType: MediaType,
                     parameters: Parameters): Boolean
}