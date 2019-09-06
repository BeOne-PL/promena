package pl.beone.promena.connector.activemq.delivery.jms

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders

internal class HeadersToSentBackDeterminer {

    fun determine(headers: Map<String, Any>): Map<String, Any> {
        return headers.filter { (key) -> key.startsWith(PromenaJmsHeaders.SEND_BACK_PREFIX) }.toMap()
    }
}
