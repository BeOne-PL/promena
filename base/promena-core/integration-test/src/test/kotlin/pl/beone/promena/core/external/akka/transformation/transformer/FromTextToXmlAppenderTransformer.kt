package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.toTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.plus

object FromTextToXmlAppenderTransformer : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
        dataDescriptor.descriptors.map { (data, _, metadata) ->
            singleTransformedDataDescriptor(data.surroundWithTag(parameters.getTag()).toMemoryData(), metadata.addTransformerId())
        }.toTransformedDataDescriptor()

    private fun Parameters.getTag(): String =
        get("tag", String::class.java)

    private fun Data.surroundWithTag(tag: String): String =
        "<$tag>" + String(getBytes()) + "</$tag>"

    private fun Metadata.addTransformerId(): Metadata =
        this + ("from&text*to(xml)appender-transformer" to true)

    override fun isSupported(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters) {
        dataDescriptor.descriptors.forEach {
            if (it.mediaType != TEXT_PLAIN) {
                throwException()
            }
        }

        if (targetMediaType != TEXT_XML) {
            throwException()
        }
    }

    private fun throwException() {
        throw TransformationNotSupportedException.custom("Only transformation from text/plain to text/xml is supported")
    }

}