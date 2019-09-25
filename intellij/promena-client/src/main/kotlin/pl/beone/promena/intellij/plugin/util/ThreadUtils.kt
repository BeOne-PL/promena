package pl.beone.promena.intellij.plugin.util

import com.intellij.openapi.application.ApplicationManager

fun invokeLater(toRun: () -> Unit) {
    ApplicationManager.getApplication().invokeLater {
        toRun()
    }
}