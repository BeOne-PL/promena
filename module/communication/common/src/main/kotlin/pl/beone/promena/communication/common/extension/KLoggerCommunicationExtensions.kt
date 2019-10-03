package pl.beone.promena.communication.common.extension

import mu.KLogger

fun KLogger.warnIfCommunicationsAreDifferent(internalCommunicationId: String, externalCommunicationId: String) {
    if (internalCommunicationId != externalCommunicationId) {
        warn { "You should use the same communication implementation for performance reasons. Now internal communication is <$internalCommunicationId> and external communication is <$externalCommunicationId>" }
    }
}