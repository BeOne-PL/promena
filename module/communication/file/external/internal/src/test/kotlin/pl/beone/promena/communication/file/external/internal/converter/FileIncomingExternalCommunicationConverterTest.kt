package pl.beone.promena.communication.file.external.internal.converter

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.internal.fileCommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor

class FileIncomingExternalCommunicationConverterTest {

    @Test
    fun `convert _ should log that external communication contains directory that is a subpath of internal directory`() {
        val directory = createTempDir()
        FileIncomingExternalCommunicationConverter(
            fileCommunicationParameters(directory, false),
            mockk { every { convert(any<DataDescriptor>(), true) } returns emptyDataDescriptor() }
        ).convert(emptyDataDescriptor(), fileCommunicationParameters(directory.resolve("sub"), false))
    }

    @Test
    fun `convert _ should log that external communication contains directory that isn't included in internal directory`() {
        FileIncomingExternalCommunicationConverter(
            fileCommunicationParameters(createTempDir(), false),
            mockk { every { convert(any<DataDescriptor>(), true) } returns emptyDataDescriptor() }
        ).convert(emptyDataDescriptor(), fileCommunicationParameters(createTempDir(), false))
    }
}