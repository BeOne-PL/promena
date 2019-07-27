package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import org.fusesource.hawtbuf.UTF8Buffer

internal fun String.toUTF8Buffer(): UTF8Buffer =
    UTF8Buffer(this)