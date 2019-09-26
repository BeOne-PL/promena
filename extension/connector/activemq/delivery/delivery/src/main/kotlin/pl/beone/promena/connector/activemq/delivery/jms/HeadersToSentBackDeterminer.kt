package pl.beone.promena.connector.activemq.delivery.jms

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_PREFIX

object HeadersToSentBackDeterminer {

    fun determine(headers: Map<String, Any>): Map<String, Any> =
        headers.filter { (key) -> key.startsWith(SEND_BACK_PREFIX) }.toMap()
}
