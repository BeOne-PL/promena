package pl.beone.promena.intellij.plugin.common

import pl.beone.promena.transformer.contract.transformation.Transformation

internal fun Class<*>.invokePromenaMethod(methodName: String): Transformation =
    getMethod(methodName).invoke(null) as Transformation
