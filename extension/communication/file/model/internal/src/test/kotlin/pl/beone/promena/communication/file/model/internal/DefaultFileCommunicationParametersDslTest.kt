package pl.beone.promena.communication.file.model.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters

class DefaultFileCommunicationParametersDslTest {

    @Test
    fun internalFileCommunicationParameters() {
        val directory = createTempDir()
        internalFileCommunicationParameters(directory).let {
            it.getId() shouldBe FileCommunicationParameters.ID
            it.getDirectory() shouldBe directory
        }
    }

    @Test
    fun externalFileCommunicationParameters() {
        val directory = createTempDir()
        externalFileCommunicationParameters(directory).let {
            it.getId() shouldBe FileCommunicationParameters.ID
            it.getDirectory() shouldBe directory
        }
    }
}