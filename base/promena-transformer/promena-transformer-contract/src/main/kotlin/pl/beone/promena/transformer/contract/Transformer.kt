package pl.beone.promena.transformer.contract

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

interface Transformer {

    @Throws(TransformerException::class, TransformerTimeoutException::class)
    fun transform(dataDescriptors: List<DataDescriptor>,
                  targetMediaType: MediaType,
                  parameters: Parameters): List<TransformedDataDescriptor>

    fun canTransform(dataDescriptors: List<DataDescriptor>,
                     targetMediaType: MediaType,
                     parameters: Parameters): Boolean
}