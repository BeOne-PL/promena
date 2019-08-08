@file:JvmName("${pascalCaseTransformerId}Dsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import pl.beone.promena.transformer.internal.model.parameters.addIfNotNull
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus

fun ${transformerName}Transformation(targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    singleTransformation(${pascalCaseTransformerId}Constants.TRANSFORMER_NAME, targetMediaType, parameters)

fun ${camelCaseTransformerId}Transformation(targetMediaType: MediaType, parameters: Parameters): Transformation.Single =
    singleTransformation(${pascalCaseTransformerId}Constants.TRANSFORMER_ID, targetMediaType, parameters)

fun ${camelCaseTransformerId}Parameters(example: String, example2: String? = null): MapParameters =
    emptyParameters() +
            (${pascalCaseTransformerId}Constants.Parameters.EXAMPLE to example) addIfNotNull
            (${pascalCaseTransformerId}Constants.Parameters.EXAMPLE2 to example2)