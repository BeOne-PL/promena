package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

fun MediaType.toPrettyString(): String =
    "($mimeType, ${charset.name()})"