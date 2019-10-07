package pl.beone.promena.connector.normal.http.delivery.extension

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

fun MediaType.toHttpString(): String =
    mimeType + "; charset=" + charset.name()