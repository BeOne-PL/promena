@file:JvmName("TransformationDsl")

package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

fun singleTransformation(transformerName: String, transformerSubName: String, targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    Transformation.Single.of(transformerName, transformerSubName, targetMediaType, parameters)

fun singleTransformation(transformerName: String, targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    Transformation.Single.of(transformerName, targetMediaType, parameters)

infix fun Transformation.Single.next(transformer: Transformation.Single): Transformation.Composite =
    Transformation.Composite.of(transformers + transformer)

fun compositeTransformation(transformer: Transformation.Single,
                            transformers: List<Transformation.Single>): Transformation.Composite =
    Transformation.Composite.of(listOf(transformer) + transformers)

fun compositeTransformation(transformer: Transformation.Single,
                            vararg transformers: Transformation.Single): Transformation.Composite =
    compositeTransformation(transformer, transformers.toList())

infix fun Transformation.Composite.next(transformer: Transformation.Single): Transformation.Composite =
    Transformation.Composite.of(transformers + transformer)

fun transformation(transformers: List<Transformation.Single>): Transformation =
    when (transformers.size) {
        0    -> throw IllegalArgumentException("Transformation must consist of at least one transformer")
        1    -> transformers.first()
        else -> Transformation.Composite.of(transformers)
    }

fun transformation(vararg transformers: Transformation.Single): Transformation =
    transformation(transformers.toList())

fun List<Transformation.Single>.toTransformation(): Transformation =
    transformation(this)