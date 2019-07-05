package pl.beone.promena.core.internal.communication.external.manager

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerException
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication

class DefaultExternalCommunicationManagerTest {

    companion object {
        private const val externalId = "external"
        private const val external2Id = "external2"
        private val externalCommunication = ExternalCommunication(externalId, mockk(), mockk())
        private val externalCommunication2 = ExternalCommunication(external2Id, mockk(), mockk())
        private val externalCommunications = listOf(externalCommunication, externalCommunication2)
    }

    @Test
    fun getCommunication() {
        DefaultExternalCommunicationManager(externalCommunications, false, externalId)
                .getCommunication(externalId) shouldBe externalCommunication
    }

    @Test
    fun `getCommunication _ should throw ExternalCommunicationManagerException`() {
        shouldThrowExactly<ExternalCommunicationManagerException> {
            DefaultExternalCommunicationManager(externalCommunications, false, externalId)
                    .getCommunication("absent")
        }.message shouldBe "There is no <absent> external communication: <[external, external2]>"
    }

    @Test
    fun `getCommunication _ back pressure`() {
        DefaultExternalCommunicationManager(externalCommunications, true, externalId)
                .getCommunication("absent") shouldBe externalCommunication
    }

    @Test
    fun `getCommunication _ absent back pressure _ should throw ExternalCommunicationManagerException`() {
        shouldThrowExactly<ExternalCommunicationManagerException> {
            DefaultExternalCommunicationManager(externalCommunications, true, "absent")
        }.message shouldBe "Couldn't determine back pressure communication"
    }
}