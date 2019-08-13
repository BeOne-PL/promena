@file:JvmName("${pascalCaseTransformerId}ParametersDsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import pl.beone.promena.transformer.internal.model.parameters.addIfNotNull
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus

fun ${camelCaseTransformerId}Parameters(example: String, example2: String? = null): MapParameters =
    emptyParameters() +
            (${pascalCaseTransformerId}ParametersConstants.EXAMPLE to example) addIfNotNull
            (${pascalCaseTransformerId}ParametersConstants.EXAMPLE2 to example2)