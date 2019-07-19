package pl.beone.promena.core.external.akka.transformation.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.data.SequentialTransformedDataDescriptors
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.add
import pl.beone.promena.transformer.internal.model.metadata.metadata

class TextAppenderTransformer : Transformer {

    override fun transform(dataDescriptors: DataDescriptors, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptors =
            SequentialTransformedDataDescriptors.of(
                    dataDescriptors.getAll().map {
                        TransformedDataDescriptor.of(it.data.addHashAtTheEnd(parameters.getAppend()).toMemoryData(),
                                                     it.metadata.addTransformerId())
                    }
            )

    private fun Parameters.getAppend(): String =
            get("append", String::class.java)

    private fun Data.addHashAtTheEnd(sign: String): String =
            String(getBytes()) + sign

    private fun Metadata.addTransformerId(): Metadata =
            metadata(getAll()) add ("text-appender-transformer" to true)

    override fun canTransform(dataDescriptors: DataDescriptors, targetMediaType: MediaType, parameters: Parameters): Boolean =
            dataDescriptors.getAll().all { it.mediaType == TEXT_PLAIN } && targetMediaType == TEXT_PLAIN

}