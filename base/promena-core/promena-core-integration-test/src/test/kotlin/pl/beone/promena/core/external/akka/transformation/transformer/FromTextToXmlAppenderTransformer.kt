package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.data.transformedDataDescriptor
import pl.beone.promena.transformer.contract.data.transformedDataDescriptors
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.plus

class FromTextToXmlAppenderTransformer : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptors =
            transformedDataDescriptors(dataDescriptor.descriptors.map {
                transformedDataDescriptor(it.data.surroundWithTag(parameters.getTag()).toMemoryData(),
                                          it.metadata.addTransformerId())
            })

    private fun Parameters.getTag(): String =
            get("tag", String::class.java)

    private fun Data.surroundWithTag(tag: String): String =
            "<$tag>" + String(getBytes()) + "</$tag>"

    private fun Metadata.addTransformerId(): Metadata =
            this + ("from-text-to-xml-appender-transformer" to true)

    override fun canTransform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): Boolean =
            dataDescriptor.descriptors.all { it.mediaType == TEXT_PLAIN } && targetMediaType == TEXT_XML

}