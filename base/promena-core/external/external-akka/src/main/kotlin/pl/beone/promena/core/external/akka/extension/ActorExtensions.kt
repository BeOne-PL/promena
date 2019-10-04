package pl.beone.promena.core.external.akka.extension

private val notLetterOrDigitRegex = "[^a-zA-Z0-9]".toRegex()

fun String.toCorrectActorName(): String =
    replace(notLetterOrDigitRegex, "-")
        .toLowerCase()