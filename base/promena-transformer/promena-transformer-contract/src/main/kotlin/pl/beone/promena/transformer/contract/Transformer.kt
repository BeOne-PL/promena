package pl.beone.promena.transformer.contract

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerCouldNotTransformException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

interface Transformer {

    @Throws(TimeoutException::class)
    fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor

    @Throws(TransformerCouldNotTransformException::class)
    fun canTransform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters)
}