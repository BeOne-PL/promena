package pl.beone.promena.intellij.plugin.classloader

import com.intellij.util.lang.UrlClassLoader
import pl.beone.promena.transformer.contract.data.DataDescriptor
import java.io.File
import java.lang.reflect.Method
import java.net.URL

internal fun loadClasses(parent: ClassLoader, path: String): UrlClassLoader =
    UrlClassLoader.build()
        .parent(parent)
        .urls(listOf(toUrl(path))).get()

private fun toUrl(path: String): URL =
    File(path).toURI().toURL()

internal fun UrlClassLoader.createClass(qualifiedClassName: String): Class<*> =
    loadClass(qualifiedClassName)

internal fun Class<*>.getPromenaMethod(methodName: String): Method =
    getMethod(methodName, DataDescriptor::class.java)
