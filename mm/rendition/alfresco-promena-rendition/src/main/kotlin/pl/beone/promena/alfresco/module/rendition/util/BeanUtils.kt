package pl.beone.promena.alfresco.module.rendition.util

import io.mockk.mockkClass

fun mock(canonicalClassName: String): Any =
    try {
        mockkClass(Class.forName(canonicalClassName).kotlin)
    } catch (e: Throwable) {
        throw IllegalStateException("Couldn't create MockK of <$canonicalClassName>", e)
    }