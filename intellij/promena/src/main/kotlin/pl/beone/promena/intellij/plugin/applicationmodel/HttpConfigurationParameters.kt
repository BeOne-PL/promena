package pl.beone.promena.intellij.plugin.applicationmodel

internal data class HttpConfigurationParameters(
    val host: String,
    val port: Int
) {

    fun getAddress(): String =
        host.removeSuffix("/") + ":" + port
}