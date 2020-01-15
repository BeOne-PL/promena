package pl.beone.promena.core.internal.communication.external.manager

import mu.KotlinLogging
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationNotFoundException
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager

/**
 * Manages [externalCommunications] in memory.
 * In case of the absence of an instance of *external communication*,
 * this implementation uses *external communication* associated with [backPressureId] if [backPressureCommunicationEnabled] is `true`.
 */
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

    private fun getBackPressureCommunication(externalCommunications: List<ExternalCommunication>, backPressureId: String): ExternalCommunication =
        externalCommunications.firstOrNull { (id) -> id == backPressureId }
            ?: error("Couldn't determine back pressure communication. " + createNotFoundExceptionMessage(backPressureId, externalCommunications))

    override fun getCommunication(id: String): ExternalCommunication =
        communicationMap[id] ?: determineCommunication(id)

    private fun determineCommunication(id: String): ExternalCommunication =
        if (backPressureCommunicationEnabled) {
            logger.warn { "There is no <$id> external communication. Using <$backPressureId> as back pressure" }
            backPressureCommunication
        } else {
            throw ExternalCommunicationNotFoundException(createNotFoundExceptionMessage(id, externalCommunications))
        }

    private fun createCommunicationMap(externalCommunications: List<ExternalCommunication>): Map<String, ExternalCommunication> =
        externalCommunications.map { it.id to it }.toMap()

    private fun createNotFoundExceptionMessage(id: String, externalCommunications: List<ExternalCommunication>): String =
        "There is no <$id> external communication: ${externalCommunications.map { it.id }}"
}