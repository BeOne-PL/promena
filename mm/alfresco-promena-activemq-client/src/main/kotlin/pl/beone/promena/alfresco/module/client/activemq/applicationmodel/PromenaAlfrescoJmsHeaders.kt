package pl.beone.promena.alfresco.module.client.activemq.applicationmodel

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders

object PromenaAlfrescoJmsHeaders {
    const val COMMUNICATION_PARAMETERS_LOCATION = "${PromenaJmsHeaders.COMMUNICATION_PARAMETERS_PREFIX}location"

    const val SEND_BACK_NODE_REFS = "${PromenaJmsHeaders.SEND_BACK_PREFIX}node_refs"
    const val SEND_BACK_NODES_CHECKSUM = "${PromenaJmsHeaders.SEND_BACK_PREFIX}nodes_checksum"
    const val SEND_BACK_USER_NAME = "${PromenaJmsHeaders.SEND_BACK_PREFIX}user_name"

    const val SEND_BACK_ATTEMPT = "${PromenaJmsHeaders.SEND_BACK_PREFIX}attempt"

    const val SEND_BACK_RETRY_ENABLED = "${PromenaJmsHeaders.SEND_BACK_PREFIX}retry_enabled"
    const val SEND_BACK_RETRY_MAX_ATTEMPTS = "${PromenaJmsHeaders.SEND_BACK_PREFIX}retry_max_attempts"
    const val SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY = "${PromenaJmsHeaders.SEND_BACK_PREFIX}retry_next_attempt_delay"
}