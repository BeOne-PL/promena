package pl.beone.promena.intellij.plugin.extension

import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import java.io.File
import java.nio.charset.Charset

private val tika = Tika()
private val tikaConfig = TikaConfig.getDefaultConfig()

fun File.detectMimeType(): String =
    tika.detect(this)

fun File.detectCharset(): Charset =
    Charset.forName(reader().encoding)

fun MediaType.determineExtension(): String? =
    tikaConfig.mimeRepository.forName(mimeType).extension.let {
        if (!it.isBlank()) {
            it
        } else {
            null
        }
    }
