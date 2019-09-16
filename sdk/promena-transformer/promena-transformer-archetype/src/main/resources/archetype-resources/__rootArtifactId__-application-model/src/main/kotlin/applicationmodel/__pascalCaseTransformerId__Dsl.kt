@file:JvmName("${pascalCaseTransformerId}Dsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants.TRANSFORMER_ID
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants.TRANSFORMER_NAME

fun ${transformerName}Transformation(targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    singleTransformation(TRANSFORMER_NAME, targetMediaType, parameters)

fun ${camelCaseTransformerId}Transformation(targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    singleTransformation(TRANSFORMER_ID, targetMediaType, parameters)