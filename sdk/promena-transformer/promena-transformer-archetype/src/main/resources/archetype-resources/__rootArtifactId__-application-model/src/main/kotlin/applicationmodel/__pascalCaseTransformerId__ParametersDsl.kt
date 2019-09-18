@file:JvmName("${pascalCaseTransformerId}ParametersDsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import pl.beone.promena.transformer.internal.model.parameters.addIfNotNull
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.EXAMPLE
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.EXAMPLE2

fun ${camelCaseTransformerId}Parameters(example: String, example2: String? = null): MapParameters =
    emptyParameters() +
            (EXAMPLE to example) addIfNotNull
            (EXAMPLE2 to example2)