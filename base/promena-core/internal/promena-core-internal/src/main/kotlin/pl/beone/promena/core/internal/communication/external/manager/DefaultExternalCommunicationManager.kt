package pl.beone.promena.core.internal.communication.external.manager

import mu.KotlinLogging
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerException
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager

class DefaultExternalCommunicationManager(
    private val externalCommunications: List<ExternalCommunication>,
    private val backPressureCommunicationEnabled: Boolean,
    private val backPressureId: String
) : ExternalCommunicationManager {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val communicationMap: Map<String, ExternalCommunication>
    private lateinit var backPressureCommunication: ExternalCommunication

    init {
        communicationMap = createCommunicationMap(externalCommunications)
        if (backPressureCommunicationEnabled) {
            backPressureCommunication = getBackPressureCommunication(externalCommunications, backPressureId)
        }
    }

    override fun getCommunication(id: String): ExternalCommunication =
        communicationMap[id]
            ?: if (backPressureCommunicationEnabled) {
                logger.warn { "There is no <$id> external communication. Using <$backPressureId> as back pressure" }
                backPressureCommunication
            } else {
                throw createException(externalCommunications, id)
            }

    private fun createCommunicationMap(externalCommunications: List<ExternalCommunication>): Map<String, ExternalCommunication> =
        externalCommunications.map { it.id to it }.toMap()

    private fun getBackPressureCommunication(externalCommunications: List<ExternalCommunication>, backPressureId: String): ExternalCommunication =
        try {
            externalCommunications.firstOrNull { (id, _, _) ->
                id == backPressureId
            } ?: throw createException(externalCommunications, backPressureId)
        } catch (e: ExternalCommunicationManagerException) {
            throw ExternalCommunicationManagerException("Couldn't determine back pressure communication", e)
        }

    private fun createException(externalCommunications: List<ExternalCommunication>, id: String): ExternalCommunicationManagerException =
        ExternalCommunicationManagerException("There is no <$id> external communication: ${externalCommunications.map { it.id }}")
}