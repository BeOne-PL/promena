package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
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

class FromTextToXmlAppenderTransformer : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
            dataDescriptor.descriptors.map { (data, _, metadata) ->
                singleTransformedDataDescriptor(data.surroundWithTag(parameters.getTag()).toMemoryData(),
                                                metadata.addTransformerId())
            }.toTransformedDataDescriptor()

    private fun Parameters.getTag(): String =
            get("tag", String::class.java)

    private fun Data.surroundWithTag(tag: String): String =
            "<$tag>" + String(getBytes()) + "</$tag>"

    private fun Metadata.addTransformerId(): Metadata =
            this + ("from-text-to-xml-appender-transformer" to true)

    override fun canTransform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): Boolean =
            dataDescriptor.descriptors.all { it.mediaType == TEXT_PLAIN } && targetMediaType == TEXT_XML

}