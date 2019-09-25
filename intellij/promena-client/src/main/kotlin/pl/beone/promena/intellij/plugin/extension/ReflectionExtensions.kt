package pl.beone.promena.intellij.plugin.extension

import pl.beone.promena.transformer.contract.transformation.Transformation

internal fun Class<*>.invokePromenaMethod(methodName: String): Transformation =
    getMethod(methodName).invoke(null) as Transformation
