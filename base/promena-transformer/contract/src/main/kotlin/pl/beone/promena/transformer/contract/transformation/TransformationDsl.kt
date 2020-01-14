@file:JvmName("TransformationDsl")

package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.TransformerId

fun singleTransformation(transformerId: TransformerId, targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    Transformation.Single.of(transformerId, targetMediaType, parameters)

fun singleTransformation(transformerName: String, transformerSubName: String, targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    Transformation.Single.of(transformerName, transformerSubName, targetMediaType, parameters)

fun singleTransformation(transformerName: String, targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    Transformation.Single.of(transformerName, targetMediaType, parameters)

/**
 * ```
 * singleTransformation("converter", APPLICATION_PDF, <Parameters>) next
 *      singleTransformation("ocr", APPLICATION_PDF, <Parameters>)
 * ```
 * @return the flow of `this` and [transformer]
 */
infix fun Transformation.Single.next(transformer: Transformation.Single): Transformation.Composite =
    Transformation.Composite.of(transformers + transformer)

fun compositeTransformation(transformer: Transformation.Single, transformers: List<Transformation.Single>): Transformation.Composite =
    Transformation.Composite.of(listOf(transformer) + transformers)

fun compositeTransformation(transformer: Transformation.Single, vararg transformers: Transformation.Single): Transformation.Composite =
    compositeTransformation(transformer, transformers.toList())

/**
 * ```
 * singleTransformation("converter", APPLICATION_PDF, <Parameters>) next
 *      singleTransformation("ocr", APPLICATION_PDF, <Parameters>) next
 *      singleTransformation("page extractor", APPLICATION_PDF, <Parameters>)
 * ```
 * @return the flow of `this` and [transformer]
 */
infix fun Transformation.Composite.next(transformer: Transformation.Single): Transformation.Composite =
    Transformation.Composite.of(transformers + transformer)

/**
 * ```
 * compositeTransformation(singleTransformation("converter", APPLICATION_PDF, <Parameters>)) next
 *      compositeTransformation(singleTransformation("ocr", APPLICATION_PDF, <Parameters>))
 * ```
 * @return the flow of `this` and [transformer]
 */
infix fun Transformation.Composite.next(transformer: Transformation.Composite): Transformation.Composite =
    Transformation.Composite.of(transformers + transformer.transformers)

/**
 * @return [Transformation.Single] if [transformers] has one element
 *         and [Transformation.Composite] if [transformers] has many elements
 * @throws IllegalArgumentException if an empty list is passed
 */
fun transformation(transformers: List<Transformation.Single>): Transformation =
    when (transformers.size) {
        0 -> throw IllegalArgumentException("Transformation must consist of at least one transformation stage")
        1 -> transformers.first()
        else -> Transformation.Composite.of(transformers)
    }

/**
 * @see [transformation]
 */
fun transformation(vararg transformers: Transformation.Single): Transformation =
    transformation(transformers.toList())

/**
 * @see [transformation]
 */
fun List<Transformation.Single>.toTransformation(): Transformation =
    transformation(this)