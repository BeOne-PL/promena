package pl.beone.promena.intellij.plugin.classloader

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.lang.UrlClassLoader
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.io.File
import java.net.URL

internal fun loadClasses(parent: ClassLoader, outputFolders: List<VirtualFile>): UrlClassLoader =
    UrlClassLoader.build()
        .parent(parent)
        .urls(outputFolders.mapToUrls()).get()

private fun List<VirtualFile>.mapToUrls(): List<URL> =
    map { it.path.toUrl() }

private fun String.toUrl(): URL =
    File(this).toURI().toURL()

internal fun UrlClassLoader.createClass(qualifiedClassName: String): Class<*> =
    loadClass(qualifiedClassName)

internal fun Class<*>.invokePromenaMethod(methodName: String): Transformation =
    getMethod(methodName).invoke(null) as Transformation
