package pl.beone.promena.intellij.plugin.extension

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunDialog
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.idea.util.projectStructure.allModules

fun Project.getActiveFile(): VirtualFile =
    getEditor().file ?: error("No file open")

fun Project.getActiveFileOrNull(): VirtualFile? =
    try {
        getActiveFile()
    } catch (e: IllegalStateException) {
        null
    }

fun Project.getEditor(): FileEditor =
    FileEditorManager.getInstance(this).selectedEditors.firstOrNull { it is TextEditor }
        ?: error("No text editor or file opened")

fun Project.getCompilerOutputFolders(): List<VirtualFile> =
    allModules().flatMap {
        try {
            it.getCompilerOutputFolders()
        } catch (e: IllegalStateException) {
            emptyList<VirtualFile>()
        }
    }

fun Project.isFileInAnyModule(file: VirtualFile): Boolean =
    getModuleForFile(file) != null

private fun Project.getModuleForFile(file: VirtualFile): Module? =
    ModuleManager.getInstance(this).modules.firstOrNull { it.moduleScope.contains(file) }

fun Project.editConfiguration(runnerAndConfigurationSettings: RunnerAndConfigurationSettings) {
    RunDialog.editConfiguration(this, runnerAndConfigurationSettings, "Edit configuration")
}