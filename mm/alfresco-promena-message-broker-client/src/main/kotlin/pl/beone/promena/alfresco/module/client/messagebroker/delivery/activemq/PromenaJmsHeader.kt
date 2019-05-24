package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

object PromenaJmsHeader {
    const val PROMENA_TRANSFORMER_ID = "promena_transformerId"
    const val PROMENA_TRANSFORMATION_START_TIMESTAMP = "promena_transformation_timestamp_start"
    const val PROMENA_TRANSFORMATION_END_TIMESTAMP = "promena_transformation_timestamp_end"
    const val PROMENA_COMMUNICATION_LOCATION = "promena_com_location"
    const val SEND_BACK_NODE_REFS = "send_back_nodeRefs"
    const val SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE = "send_back_targetMediaType_mimeType"
    const val SEND_BACK_TARGET_MEDIA_TYPE_CHARSET = "send_back_targetMediaType_charset"
    const val SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS = "send_back_parameters"
}