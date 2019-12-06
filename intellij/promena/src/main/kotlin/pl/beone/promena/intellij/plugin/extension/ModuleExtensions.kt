package pl.beone.promena.intellij.plugin.extension

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.vfs.VirtualFile

fun Project.getActiveModule(): Module =
    getModule(getActiveFile())

fun Project.getModule(file: VirtualFile): Module =
    getModuleForFile(file) ?: error("No active module")

private fun Project.getModuleForFile(file: VirtualFile): Module? =
    ModuleManager.getInstance(this).modules.firstOrNull { it.moduleScope.contains(file) }

fun Module.getCompilerOutputFolders(): List<VirtualFile> {
    return CompilerModuleExtension.getInstance(this)
        ?.getOutputRoots(true)
        ?.toList()
        ?: error("There is no CompilerModuleExtension instance available")
}

fun Module.getCompilerOutputFolder(): VirtualFile {
    return CompilerModuleExtension.getInstance(this)
        ?.compilerOutputPath
        ?: error("There is no CompilerModuleExtension instance available or compiler output folder isn't set")
}