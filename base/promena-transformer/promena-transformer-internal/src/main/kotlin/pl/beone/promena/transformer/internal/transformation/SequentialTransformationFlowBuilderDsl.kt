package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor

fun transformationFlow(transformerDescriptor: TransformerDescriptor): SequentialTransformationFlow =
        SequentialTransformationFlow.of(transformerDescriptor)

fun transformationFlow(id: String, targetMediaType: MediaType, parameters: Parameters): SequentialTransformationFlow =
        SequentialTransformationFlow.of(TransformerDescriptor.of(id, targetMediaType, parameters))

infix fun SequentialTransformationFlow.next(transformerDescriptor: TransformerDescriptor): SequentialTransformationFlow =
        SequentialTransformationFlow.of(getAll() + transformerDescriptor)

fun SequentialTransformationFlow.next(id: String, targetMediaType: MediaType, parameters: Parameters): SequentialTransformationFlow =
        SequentialTransformationFlow.of(getAll() + TransformerDescriptor.of(id, targetMediaType, parameters))