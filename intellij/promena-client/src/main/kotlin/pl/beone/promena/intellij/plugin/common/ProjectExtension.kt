package pl.beone.promena.intellij.plugin.common

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun Project.getActiveFile(): VirtualFile =
    getEditor().file ?: throw IllegalStateException("No file open")

fun Project.getEditor(): FileEditor =
    FileEditorManager.getInstance(this).selectedEditors.firstOrNull { it is TextEditor }
        ?: throw IllegalStateException("No text editor or file opened")