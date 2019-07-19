package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

class TimeoutTransformer : Transformer {

    override fun transform(dataDescriptors: DataDescriptors, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptors {
        Thread.sleep(parameters.getTimeout().toMillis())
        throw TimeoutException()
    }

    override fun canTransform(dataDescriptors: DataDescriptors, targetMediaType: MediaType, parameters: Parameters): Boolean =
            true

}