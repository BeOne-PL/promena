package pl.beone.promena.core.external.akka.util

internal fun <T> measureTimeMillisWithContent(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to System.currentTimeMillis() - start
}