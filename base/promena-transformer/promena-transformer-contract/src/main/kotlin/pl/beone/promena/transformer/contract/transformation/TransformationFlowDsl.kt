package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

fun singleTransformationFlow(id: String, targetMediaType: MediaType, parameters: Parameters): TransformationFlow.Single =
        TransformationFlow.Single(id, targetMediaType, parameters)

infix fun TransformationFlow.Single.next(transformer: TransformationFlow.Single): TransformationFlow.Composite =
        TransformationFlow.Composite(transformers + transformer)

fun compositeTransformationFlow(transformer: TransformationFlow.Single,
                                transformers: List<TransformationFlow.Single>): TransformationFlow.Composite =
        TransformationFlow.Composite(listOf(transformer) + transformers)

infix fun TransformationFlow.Composite.next(transformer: TransformationFlow.Single): TransformationFlow.Composite =
        TransformationFlow.Composite(transformers + transformer)

fun transformationFlow(transformers: List<TransformationFlow.Single>): TransformationFlow =
        when (transformers.size) {
            0    -> throw IllegalArgumentException("You have pass at least one transformers")
            1    -> transformers.first()
            else -> TransformationFlow.Composite.of(transformers)
        }

