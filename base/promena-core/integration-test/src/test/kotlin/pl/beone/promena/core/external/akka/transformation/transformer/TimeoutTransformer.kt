package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

object TimeoutTransformer : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor {
        Thread.sleep(parameters.getTimeout().toMillis())
        throw TimeoutException()
    }

    override fun isSupported(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters) {

    }

}