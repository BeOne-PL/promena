package pl.beone.promena.connector.messagebroker.delivery.jms

internal class HeadersToSentBackDeterminer {

    companion object {
        private const val PROMENA_SEND_BACK_PREFIX = "send_back_"
    }

    fun determine(headers: Map<String, Any>): Map<String, Any> {
        return headers.filter { (key, _) -> key.startsWith(PROMENA_SEND_BACK_PREFIX) }.toMap()
    }
}
