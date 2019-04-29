package pl.beone.promena.core.usecase.transformation

import org.slf4j.LoggerFactory
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MirrorTransformer : Transformer {

    companion object {
        private val logger = LoggerFactory.getLogger(MirrorTransformer::class.java)
    }

    override fun transform(dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> {
        logger.info("Waiting <100> milliseconds...")
        Thread.sleep(100)

        return dataDescriptors.map { TransformedDataDescriptor(it.data, MapMetadata.empty()) }
    }

    override fun canTransform(dataDescriptors: List<DataDescriptor>, targetMediaType: MediaType, parameters: Parameters): Boolean =
            true
}