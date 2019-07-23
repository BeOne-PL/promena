package pl.beone.promena.core.internal.communication.external.manager

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
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

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.core.internal.communication.external.manager.DefaultExternalCommunicationManager") as Logger).level = Level.DEBUG

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