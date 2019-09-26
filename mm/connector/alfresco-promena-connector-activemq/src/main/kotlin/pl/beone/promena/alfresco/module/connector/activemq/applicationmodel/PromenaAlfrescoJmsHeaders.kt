package pl.beone.promena.alfresco.module.connector.activemq.applicationmodel

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_PREFIX

object PromenaAlfrescoJmsHeaders {

    const val SEND_BACK_TRANSFORMATION_PARAMETERS = "${SEND_BACK_PREFIX}transformation_parameters"
    const val SEND_BACK_TRANSFORMATION_PARAMETERS_STRING = "${SEND_BACK_PREFIX}transformation_parameters_string"

    const val SEND_BACK_ATTEMPT = "${SEND_BACK_PREFIX}attempt"
    const val SEND_BACK_RETRY_MAX_ATTEMPTS = "${SEND_BACK_PREFIX}retry_max_attempts"
}