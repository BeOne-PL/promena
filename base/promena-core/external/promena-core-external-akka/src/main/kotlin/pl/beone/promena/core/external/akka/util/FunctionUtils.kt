package pl.beone.promena.core.external.akka.util

internal fun List<ByteArray>.toMB(): Double = this.sumByDouble { it.toMB() }

internal fun ByteArray.toMB(): Double = this.size.toDouble() / 1024 / 1024

internal fun Double.format(digits: Int) = String.format("%.${digits}f", this)

internal fun Long.toSeconds() = this.toDouble() / 1000

internal fun <T> measureTimeMillisWithContent(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to System.currentTimeMillis() - start
}

internal inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java
