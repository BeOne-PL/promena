package pl.beone.promena.core.external.akka.extension

internal fun Double.format(digits: Int): String =
    String.format("%.${digits}f", this)

internal fun Long.toSeconds(): Double =
    this.toDouble() / 1000