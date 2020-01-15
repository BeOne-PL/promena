package pl.beone.promena.core.contract.communication.external.manager

import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter

/**
 * Provides a full description of *external communication*.
 */
data class ExternalCommunication(
    val id: String,
    val incomingExternalCommunicationConverter: IncomingExternalCommunicationConverter,
    val outgoingExternalCommunicationConverter: OutgoingExternalCommunicationConverter
)