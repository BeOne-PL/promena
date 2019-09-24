@file:JvmName("${pascalCaseTransformerId}ParametersDsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import pl.beone.promena.transformer.internal.model.parameters.addIfNotNull
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Example
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Example2

fun ${camelCaseTransformerId}Parameters(example: String, example2: String? = null): MapParameters =
    emptyParameters() +
            (Example.NAME to example) addIfNotNull
            (Example2.NAME to example2)

fun Parameters.getExample(): String =
    get(Example.NAME, Example.CLASS)

fun Parameters.getExample2(): String =
    get(Example2.NAME, Example2.CLASS)