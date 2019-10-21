package pl.beone.promena.transformer.applicationmodel.extension

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

fun MediaType.toPrettyString(): String =
    "($mimeType, ${charset.name()})"