package pl.beone.promena.core.common.utils

fun List<ByteArray>.toMB(): Double = this.sumByDouble { it.toMB() }

fun ByteArray.toMB(): Double = this.size.toDouble() / 1024 / 1024

fun Double.format(digits: Int) = String.format("%.${digits}f", this)

fun Long.toSeconds() = this.toDouble() / 1000

fun <T> measureTimeMillisWithContent(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to System.currentTimeMillis() - start
}

inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java
