package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.toTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.plus

class TextAppenderTransformer : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
            dataDescriptor.descriptors.map { (data, _, metadata) ->
                singleTransformedDataDescriptor(data.addHashAtTheEnd(parameters.getAppend()).toMemoryData(),
                                                metadata.addTransformerId())
            }.toTransformedDataDescriptor()

    private fun Parameters.getAppend(): String =
            get("append", String::class.java)

    private fun Data.addHashAtTheEnd(sign: String): String =
            String(getBytes()) + sign

    private fun Metadata.addTransformerId(): Metadata =
            this + ("text-appender-transformer" to true)

    override fun canTransform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): Boolean =
            dataDescriptor.descriptors.all { it.mediaType == TEXT_PLAIN } && targetMediaType == TEXT_PLAIN

}