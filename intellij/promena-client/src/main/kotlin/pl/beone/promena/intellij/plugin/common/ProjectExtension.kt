package pl.beone.promena.intellij.plugin.common

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

internal fun Project.getFileName(): String =
    FileEditorManager.getInstance(this).selectedFiles.firstOrNull()?.name ?: throw IllegalStateException("No file is opened")