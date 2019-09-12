package pl.beone.promena.communication.file.model.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters

class DefaultFileCommunicationParametersDslTest {

    @Test
    fun fileCommunicationParameters() {
        val directory = createTempDir()
        fileCommunicationParameters(directory).let {
            it.getId() shouldBe FileCommunicationParameters.ID
            it.getDirectory() shouldBe directory
        }
    }
}