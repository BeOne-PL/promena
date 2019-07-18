package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor

// TODO verify how it will be looked in Java
fun transformationFlow(transformerDescriptor: TransformerDescriptor): SingleTransformationFlow =
        SingleTransformationFlow.of(transformerDescriptor)

fun transformationFlow(id: String, targetMediaType: MediaType, parameters: Parameters): SingleTransformationFlow =
        SingleTransformationFlow.of(TransformerDescriptor.of(id, targetMediaType, parameters))

fun CompositeTransformationFlow.then(transformerDescriptor: TransformerDescriptor): CompositeTransformationFlow =
        CompositeTransformationFlow.of(getAll() + transformerDescriptor)

fun CompositeTransformationFlow.then(id: String, targetMediaType: MediaType, parameters: Parameters): CompositeTransformationFlow =
        CompositeTransformationFlow.of(getAll() + TransformerDescriptor.of(id, targetMediaType, parameters))