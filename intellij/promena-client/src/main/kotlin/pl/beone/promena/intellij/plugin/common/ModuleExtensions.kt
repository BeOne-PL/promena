package pl.beone.promena.intellij.plugin.common

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.vfs.VirtualFile

fun Module.getOutputFolder(): VirtualFile =
    CompilerModuleExtension.getInstance(this)!!.compilerOutputPath ?: throw IllegalStateException("Module <$this> has no output path")