package pl.beone.promena.communication.file.common.extension

import java.io.File
import java.net.URI

fun File.isTheSamePath(file: File): Boolean =
    path == file.path

fun File.isSubPath(file: File): Boolean =
    path.startsWith(file.path)

fun File.notIncludedInPath(file: File): Boolean =
    !this.isTheSamePath(file) || !this.isSubPath(file)

fun URI.toFile(): File =
    File(this)