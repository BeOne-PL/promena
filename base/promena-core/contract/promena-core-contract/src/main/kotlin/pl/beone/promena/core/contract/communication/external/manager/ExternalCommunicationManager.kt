package pl.beone.promena.core.contract.communication.external.manager

import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerException

interface ExternalCommunicationManager {

    @Throws(ExternalCommunicationManagerException::class)
    fun getCommunication(id: String): ExternalCommunication
}