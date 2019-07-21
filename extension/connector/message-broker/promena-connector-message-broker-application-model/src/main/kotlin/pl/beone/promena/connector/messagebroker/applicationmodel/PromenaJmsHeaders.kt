package pl.beone.promena.connector.messagebroker.applicationmodel

import pl.beone.promena.transformer.contract.communication.CommunicationParameters

object PromenaJmsHeaders {

    const val TRANSFORMATION_ID = "promena_transformation_id"
    const val TRANSFORMATION_START_TIMESTAMP = "promena_transformation_timestamp_start"
    const val TRANSFORMATION_END_TIMESTAMP = "promena_transformation_timestamp_end"

    const val COMMUNICATION_PARAMETERS_PREFIX = "promena_communication_parameter_"
    const val COMMUNICATION_PARAMETERS_ID = "$COMMUNICATION_PARAMETERS_PREFIX${CommunicationParameters.Id}"

    const val SEND_BACK_PREFIX = "send_back_"

}