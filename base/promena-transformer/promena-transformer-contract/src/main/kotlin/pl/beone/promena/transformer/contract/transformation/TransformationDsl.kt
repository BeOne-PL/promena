@file:JvmName("TransformationDsl")

package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

fun singleTransformation(id: String, targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
        Transformation.Single(id, targetMediaType, parameters)

infix fun Transformation.Single.next(transformer: Transformation.Single): Transformation.Composite =
        Transformation.Composite(transformers + transformer)

fun compositeTransformation(transformer: Transformation.Single,
                            transformers: List<Transformation.Single>): Transformation.Composite =
        Transformation.Composite(listOf(transformer) + transformers)

infix fun Transformation.Composite.next(transformer: Transformation.Single): Transformation.Composite =
        Transformation.Composite(transformers + transformer)

fun transformation(transformers: List<Transformation.Single>): Transformation =
        when (transformers.size) {
            0    -> throw IllegalArgumentException("Transformation has to consist of at least one transformer")
            1    -> transformers.first()
            else -> Transformation.Composite.of(transformers)
        }

fun transformation(vararg transformers: Transformation.Single): Transformation =
        transformation(transformers.toList())
