package pl.beone.promena.communication.external.memory.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.communication.external.file.internal.FileOutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.io.File

class FileOutgoingExternalCommunicationConverterTest {

    companion object {
        private val location = createTempDir().toURI()
        private val communicationParameters = MapCommunicationParameters.create("file", mapOf("location" to location))
        private val metadata = MapMetadata(mapOf("key" to "value"))
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.file.internal.FileOutgoingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun `convert _ the same id communication parameter`() {
        val transformedDataDescriptors = listOf(TransformedDataDescriptor("test".toFileData(location), metadata))

        FileOutgoingExternalCommunicationConverter(communicationParameters)
                .convert(transformedDataDescriptors, communicationParameters) shouldBe transformedDataDescriptors
    }

    @Test
    fun `convert _ should convert Data to FileData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        val internalCommunicationParameters = MapCommunicationParameters.create("different")

        FileOutgoingExternalCommunicationConverter(internalCommunicationParameters)
                .convert(transformedDataDescriptors, communicationParameters).let {
                    it shouldHaveSize 1

                    val transformedDataDescriptor = it.first()
                    transformedDataDescriptor.data should instanceOf(FileData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe bytes
                    transformedDataDescriptor.metadata shouldBe metadata
                }
    }

    @Test
    fun `convert _ delete throws DataDeleteException _ should convert Data to FileData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } throws DataDeleteException("Exception")
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        val internalCommunicationParameters = MapCommunicationParameters.create("different")

        FileOutgoingExternalCommunicationConverter(internalCommunicationParameters)
                .convert(transformedDataDescriptors, communicationParameters).let {
                    it shouldHaveSize 1

                    val dataDescriptor = it.first()
                    dataDescriptor.data should instanceOf(FileData::class)
                    dataDescriptor.data.getBytes() shouldBe bytes
                    dataDescriptor.metadata shouldBe metadata
                }
    }

    @Test
    fun `convert _ FileData but in different location _ should convert new FileData`() {
        val text = "converted test"

        val internalCommunicationLocation = createTempDir().toURI()
        val fileData = text.toFileData(internalCommunicationLocation)

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(fileData, metadata))

        val internalCommunicationParameters = MapCommunicationParameters.create("file", mapOf("location" to internalCommunicationLocation))

        FileOutgoingExternalCommunicationConverter(internalCommunicationParameters)
                .convert(transformedDataDescriptors, communicationParameters).let {
                    it shouldHaveSize 1

                    val transformedDataDescriptor = it.first()
                    transformedDataDescriptor.data should instanceOf(FileData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe text.toByteArray()
                    transformedDataDescriptor.metadata shouldBe metadata

                    File(fileData.getLocation()).exists() shouldBe false
                }
    }
}