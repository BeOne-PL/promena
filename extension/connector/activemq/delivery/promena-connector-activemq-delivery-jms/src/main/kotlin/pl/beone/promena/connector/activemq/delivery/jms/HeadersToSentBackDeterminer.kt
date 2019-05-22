package pl.beone.promena.connector.activemq.delivery.jms

internal class HeadersToSentBackDeterminer {

    fun determine(headers: Map<String, Any>): Map<String, Any> =
            headers.filter { (key, _) -> key.startsWith("send_back_") }.toMap()
}
