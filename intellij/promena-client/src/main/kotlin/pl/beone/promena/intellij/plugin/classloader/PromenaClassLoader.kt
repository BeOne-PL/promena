package pl.beone.promena.intellij.plugin.classloader

import com.intellij.util.lang.UrlClassLoader
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.io.File
import java.net.URL

internal fun loadClasses(parent: ClassLoader, path: String): UrlClassLoader =
    UrlClassLoader.build()
        .parent(parent)
        .urls(listOf(toUrl(path))).get()

private fun toUrl(path: String): URL =
    File(path).toURI().toURL()

internal fun UrlClassLoader.createClass(qualifiedClassName: String): Class<*> =
    loadClass(qualifiedClassName)

internal fun Class<*>.invokePromenaMethod(methodName: String): Transformation =
    getMethod(methodName).invoke(null) as Transformation
