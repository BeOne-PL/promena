package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class MediaTypeConverter {

    fun convert(mimeType: String, charset: String): MediaType =
            MediaType.create(mimeType, charset(charset))
}