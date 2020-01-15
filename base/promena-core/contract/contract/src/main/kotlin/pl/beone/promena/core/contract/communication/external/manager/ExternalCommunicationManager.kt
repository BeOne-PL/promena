package pl.beone.promena.core.contract.communication.external.manager

import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationNotFoundException

interface ExternalCommunicationManager {

    /**
     * @throws ExternalCommunicationNotFoundException if there is no *external communication* with [id]
     */
    fun getCommunication(id: String): ExternalCommunication
}