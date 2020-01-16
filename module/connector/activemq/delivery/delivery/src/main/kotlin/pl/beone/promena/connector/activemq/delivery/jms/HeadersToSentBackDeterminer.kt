package pl.beone.promena.connector.activemq.delivery.jms

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_PREFIX

internal object HeadersToSentBackDeterminer {

    /**
     * @return [headers] whose keys start with [SEND_BACK_PREFIX].
     */
    fun determine(headers: Map<String, Any>): Map<String, Any> =
        headers.filter { (key) -> key.startsWith(SEND_BACK_PREFIX) }.toMap()
}
