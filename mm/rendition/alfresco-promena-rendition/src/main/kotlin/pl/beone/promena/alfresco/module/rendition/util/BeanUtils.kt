package pl.beone.promena.alfresco.module.rendition.util

import io.mockk.mockkClass

fun mock(canonicalClassName: String): Any =
    mockkClass(Class.forName(canonicalClassName).kotlin)