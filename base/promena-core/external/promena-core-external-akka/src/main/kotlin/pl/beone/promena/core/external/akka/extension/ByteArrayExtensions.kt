package pl.beone.promena.core.external.akka.extension

internal fun List<ByteArray>.toMB(): Double =
    this.sumByDouble { it.toMB() }

internal fun ByteArray.toMB(): Double =
    this.size.toDouble() / 1024 / 1024