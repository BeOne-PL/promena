package pl.beone.promena.transformer.internal.extension

fun List<ByteArray>.toMB(): Double =
    this.sumByDouble { it.toMB() }

fun ByteArray.toMB(): Double =
    this.size.toDouble() / 1024 / 1024