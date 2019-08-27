package pl.beone.promena.intellij.plugin.common

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.vfs.VirtualFile

fun Project.isFileInAnyModule(file: VirtualFile): Boolean =
    getModuleForFile(file) != null

fun Project.getModule(file: VirtualFile): Module =
    getModuleForFile(file) ?: throw IllegalStateException("No active module")

fun Module.getOutputFolderFile(): VirtualFile =
    CompilerModuleExtension.getInstance(this)!!.compilerOutputPath ?: throw IllegalStateException("Module <$this> has no output path")

private fun Project.getModuleForFile(file: VirtualFile): Module? =
    ModuleManager.getInstance(this).modules.firstOrNull { it.moduleScope.contains(file) }
