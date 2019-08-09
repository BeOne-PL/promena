package pl.beone.promena.communication.utils.logger

import mu.KLogger

fun KLogger.warnIfCommunicationsAreDifferent(internalCommunicationId: String, externalCommunicationId: String) {
    if (internalCommunicationId != externalCommunicationId) {
        warn { "You should use the same communication implementation for performance reasons. Now internal communication implementation is <$internalCommunicationId> and external communication id is <$externalCommunicationId>" }
    }
}