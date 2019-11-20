@file:JvmName("${pascalCaseTransformerId}ParametersDsl")

package ${package}.applicationmodel

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import pl.beone.promena.transformer.internal.model.parameters.addIfNotNull
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Mandatory
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Optional
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.OptionalLimitedValue

fun ${camelCaseTransformerId}Parameters(mandatory: String, optional: String? = null, optionalLimitedValue: Int? = null): MapParameters =
    emptyParameters() +
            (Mandatory.NAME to mandatory) addIfNotNull
            (Optional.NAME to optional) addIfNotNull
            (OptionalLimitedValue.NAME to optionalLimitedValue)

fun Parameters.getMandatory(): String =
    get(Mandatory.NAME, Mandatory.CLASS)

fun Parameters.getOptional(): String =
    get(Optional.NAME, Optional.CLASS)

fun Parameters.getOptionalOrNull(): String? =
    getOrNull(Optional.NAME, Optional.CLASS)

fun Parameters.getOptionalOrDefault(default: String): String =
    getOrDefault(Optional.NAME, Optional.CLASS, default)

fun Parameters.getOptionalLimitedValue(): Int =
    get(OptionalLimitedValue.NAME, OptionalLimitedValue.CLASS)

fun Parameters.getOptionalLimitedValueOrNull(): Int? =
    getOrNull(OptionalLimitedValue.NAME, OptionalLimitedValue.CLASS)

fun Parameters.getOptionalLimitedValueOrDefault(default: Int): Int =
    getOrDefault(OptionalLimitedValue.NAME, OptionalLimitedValue.CLASS, default)