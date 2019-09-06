package pl.beone.promena.core.contract.communication.external.manager

import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerValidationException

interface ExternalCommunicationManager {

    @Throws(ExternalCommunicationManagerValidationException::class)
    fun getCommunication(id: String): ExternalCommunication
}