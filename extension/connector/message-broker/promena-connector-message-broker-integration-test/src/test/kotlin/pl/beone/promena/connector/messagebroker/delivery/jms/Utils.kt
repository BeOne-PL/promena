package pl.beone.promena.connector.messagebroker.delivery.jms

import pl.beone.promena.transformer.internal.model.data.InMemoryData

fun String.toInMemoryData(): InMemoryData =
        InMemoryData(this.trim().toByteArray())

fun getTimestamp(): Long =
        System.currentTimeMillis()