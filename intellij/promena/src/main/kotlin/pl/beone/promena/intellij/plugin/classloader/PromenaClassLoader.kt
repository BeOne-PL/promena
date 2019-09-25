package pl.beone.promena.intellij.plugin.classloader

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.lang.UrlClassLoader
import java.io.File
import java.net.URL

internal fun createClassLoaderBasedOnFoldersWithCompiledFiles(parent: ClassLoader, folders: List<VirtualFile>): UrlClassLoader =
    UrlClassLoader.build()
        .parent(parent)
        .urls(mapToUrls(folders)).get()

private fun mapToUrls(files: List<VirtualFile>): List<URL> =
    files.map { it.path.toUrl() }

private fun String.toUrl(): URL =
    File(this).toURI().toURL()