package pl.beone.promena.intellij.plugin.common

import org.apache.tika.Tika
import java.io.File
import java.nio.charset.Charset

private val tika = Tika()

fun File.detectMimeType(): String =
    tika.detect(this)

fun File.detectCharset(): Charset =
    Charset.forName(reader().encoding)