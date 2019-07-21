package pl.beone.promena.connector.messagebroker.delivery.jms

import pl.beone.promena.connector.messagebroker.applicationmodel.PromenaJmsHeaders

internal class HeadersToSentBackDeterminer {

    fun determine(headers: Map<String, Any>): Map<String, Any> {
        return headers.filter { (key, _) -> key.startsWith(PromenaJmsHeaders.SEND_BACK_PREFIX) }.toMap()
    }
}
