package ${package}

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

class ${transformerClassName} : Transformer {

    override fun transform(dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canTransform(dataDescriptors: List<DataDescriptor>,
                              targetMediaType: MediaType,
                              parameters: Parameters): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}