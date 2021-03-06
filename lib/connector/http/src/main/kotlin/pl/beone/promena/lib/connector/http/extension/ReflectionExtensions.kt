package pl.beone.promena.lib.connector.http.extension

import java.lang.reflect.Modifier

fun Throwable.isInstanceOfInnerPrivateStaticClass(): Boolean =
    try {
        javaClass.isMemberClass && Modifier.isPrivate(javaClass.modifiers)
    } catch (e: Exception) {
        false
    }