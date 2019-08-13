@file:JvmName("${pascalCaseTransformerId}Dsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.singleTransformation

fun ${transformerName}Transformation(targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    singleTransformation(${pascalCaseTransformerId}Constants.TRANSFORMER_NAME, targetMediaType, parameters)

fun ${camelCaseTransformerId}Transformation(targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    singleTransformation(${pascalCaseTransformerId}Constants.TRANSFORMER_ID, targetMediaType, parameters)