package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.matchers.shouldBeInRange

fun getTimestamp(): Long =
    System.currentTimeMillis()

fun validateTimestamps(transformationStartTimestamp: Long, transformationEndTimestamp: Long, startTimestamp: Long, endTimestamp: Long) {
    transformationStartTimestamp.let {
        it.shouldBeInRange(startTimestamp..endTimestamp)
        it shouldBeLessThan transformationEndTimestamp
    }

    transformationEndTimestamp.let {
        it.shouldBeInRange(startTimestamp..endTimestamp)
        it shouldBeGreaterThan transformationStartTimestamp
    }
}