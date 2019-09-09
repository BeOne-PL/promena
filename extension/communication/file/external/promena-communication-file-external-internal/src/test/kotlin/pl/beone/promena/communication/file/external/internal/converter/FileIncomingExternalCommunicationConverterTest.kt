package pl.beone.promena.communication.file.external.internal.converter

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus

class FileIncomingExternalCommunicationConverterTest {

    @Test
    fun `convert _ no external communication directory parameter _ should throw CommunicationParametersValidationException`() {
        shouldThrow<CommunicationParametersValidationException> {
            FileIncomingExternalCommunicationConverter(
                "file",
                "file",
                communicationParameters("file") + ("directory" to createTempDir()),
                mockk()
            ).convert(emptyDataDescriptor(), communicationParameters("file"))
        }.message shouldBe "Communication <file>: parameter <directoryPath> is mandatory"
    }
}