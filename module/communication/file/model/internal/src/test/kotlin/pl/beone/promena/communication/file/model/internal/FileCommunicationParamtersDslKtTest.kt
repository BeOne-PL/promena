package pl.beone.promena.communication.file.model.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants

class FileCommunicationParamtersDslKtTest {

    @Test
    fun fileCommunicationParameters() {
        val directory = createTempDir() // TODO update
        with(fileCommunicationParameters(directory, directory, directory, false)) {
            getId() shouldBe FileCommunicationParametersConstants.ID
            getDirectory() shouldBe directory
        }
    }
}