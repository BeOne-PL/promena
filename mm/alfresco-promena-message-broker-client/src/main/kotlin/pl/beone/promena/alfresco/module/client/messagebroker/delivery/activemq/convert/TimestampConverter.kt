package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import java.time.Instant.ofEpochMilli
import java.time.LocalDateTime
import java.time.ZoneId


class TimestampConverter {

    fun convert(timestamp: Long): LocalDateTime =
            with(ofEpochMilli(timestamp)) {
                atZone(ZoneId.systemDefault()).toLocalDateTime()
            }
}