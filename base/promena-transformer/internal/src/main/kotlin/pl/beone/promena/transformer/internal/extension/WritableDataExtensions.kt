package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.model.data.WritableData
import java.io.InputStream

fun WritableData.copy(inputStream: InputStream) {
    getOutputStream().use { inputStream.copyTo(it) }
}