package pl.beone.promena.core.contract.communication.external.manager

import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerValidationException

interface ExternalCommunicationManager {

    /**
     * @throws ExternalCommunicationManagerValidationException if there is no [id] *external communication*
     */
    fun getCommunication(id: String): ExternalCommunication
}