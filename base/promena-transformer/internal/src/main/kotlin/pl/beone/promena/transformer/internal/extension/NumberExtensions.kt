package pl.beone.promena.transformer.internal.extension

fun Double.format(digits: Int): String =
    String.format("%.${digits}f", this)

fun Long.toSeconds(): Double =
    this.toDouble() / 1000