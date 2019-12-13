package pl.beone.promena.intellij.plugin.extension

import org.apache.commons.lang3.exception.ExceptionUtils

fun Throwable.toFullString(): String =
    ExceptionUtils.getStackTrace(this)