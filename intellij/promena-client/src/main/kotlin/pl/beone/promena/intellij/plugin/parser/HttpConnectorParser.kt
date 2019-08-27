package pl.beone.promena.intellij.plugin.parser

internal class HttpConnectorParser {

    companion object {
        private val httpRegex = "HTTP:(.*)".toRegex()
    }

    fun parseAddress(comments: List<String>): String =
        comments.mapNotNull { httpRegex.find(it) }
            .map {
                val (address) = it.destructured
                address.trim()
            }
            .firstOrNull()
            ?: throw IllegalStateException(
                "No HTTP connector address. Correct format: " +
                        "// HTTP: <host>:<port>, " +
                        "for example: // HTTP: localhost:8080"
            )

}