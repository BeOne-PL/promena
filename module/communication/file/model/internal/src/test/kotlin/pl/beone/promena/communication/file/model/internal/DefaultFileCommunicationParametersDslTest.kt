package pl.beone.promena.communication.file.model.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters

class DefaultFileCommunicationParametersDslTest {

    @Test
    fun fileCommunicationParameters() {
        val directory = createTempDir()
        with(fileCommunicationParameters(directory)) {
            getId() shouldBe FileCommunicationParameters.ID
            getDirectory() shouldBe directory
        }
    }
}