package pl.beone.promena.connector.messagebroker.delivery.jms

import pl.beone.promena.transformer.internal.model.data.MemoryData

fun String.toMemoryData(): MemoryData =
        MemoryData(this.trim().toByteArray())

fun getTimestamp(): Long =
        System.currentTimeMillis()