package pl.beone.promena.intellij.plugin.parser

internal class ParametersParser {

    companion object {
        private val repeatRegex = """Repeat:[ ]*(\d+)""".toRegex()
        private val concurrencyRegex = """Concurrency:[ ]*(\d+)""".toRegex()
    }

    fun parse(comments: List<String>): Parameters =
        Parameters(determineRepeat(comments) ?: 1, determineConcurrency(comments) ?: 1)

    fun determineRepeat(comments: List<String>): Int? =
        comments.mapNotNull { repeatRegex.find(it) }
            .map { it.groupValues[1].toInt() }
            .firstOrNull()

    fun determineConcurrency(comments: List<String>): Int? =
        comments.mapNotNull { concurrencyRegex.find(it) }
            .map { it.groupValues[1].toInt() }
            .firstOrNull()
}