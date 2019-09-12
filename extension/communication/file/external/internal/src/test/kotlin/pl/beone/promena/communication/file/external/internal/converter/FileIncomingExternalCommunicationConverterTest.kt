package pl.beone.promena.communication.file.external.internal.converter

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.internal.externalFileCommunicationParameters
import pl.beone.promena.communication.file.model.internal.internalFileCommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor

class FileIncomingExternalCommunicationConverterTest {

    @Test
    fun `convert _ should log that external communication contains directory that is a subpath of internal directory`() {
        val directory = createTempDir()
        FileIncomingExternalCommunicationConverter(
            internalFileCommunicationParameters(directory),
            mockk { every { convert(any<DataDescriptor>()) } returns emptyDataDescriptor() }
        ).convert(emptyDataDescriptor(), externalFileCommunicationParameters(directory.resolve("sub")))
    }

    @Test
    fun `convert _ should log that external communication contains directory that ins't included in internal directory`() {
        FileIncomingExternalCommunicationConverter(
            internalFileCommunicationParameters(createTempDir()),
            mockk { every { convert(any<DataDescriptor>()) } returns emptyDataDescriptor() }
        ).convert(emptyDataDescriptor(), externalFileCommunicationParameters(createTempDir()))
    }
}