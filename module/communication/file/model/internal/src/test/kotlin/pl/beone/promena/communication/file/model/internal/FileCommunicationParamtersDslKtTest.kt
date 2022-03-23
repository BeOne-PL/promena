package pl.beone.promena.communication.file.model.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants

class FileCommunicationParamtersDslKtTest {

    @Test
    fun fileBasicCommunicationParameters() {
        val directory = createTempDir()
        with(fileCommunicationParameters(directory, false)) {
            getId() shouldBe FileCommunicationParametersConstants.ID
            getDirectory() shouldBe directory
            getIsSourceFileVolumeMounted() shouldBe false
        }
    }

    @Test
    fun fileExtendedCommunicationParameters() {
        val tempFileDirectory = createTempDir()
        val sourceFileVolumeMountDirectory = createTempDir()
        val externalSourceFileVolumeMountDirectory = createTempDir()
        with(fileCommunicationParameters(tempFileDirectory, sourceFileVolumeMountDirectory,
            externalSourceFileVolumeMountDirectory, true)) {
            getId() shouldBe FileCommunicationParametersConstants.ID
            getDirectory() shouldBe tempFileDirectory
            getIsSourceFileVolumeMounted() shouldBe true
            getSourceFileVolumeMountDirectory() shouldBe sourceFileVolumeMountDirectory
            getSourceFileVolumeExternalMountDirectory() shouldBe externalSourceFileVolumeMountDirectory
        }
    }
}