package pl.beone.promena.communication.file.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

class FileCommunicationValidatorTest {

    @Test
    fun validate() {
        val dataDescriptor = DataDescriptor(mockk {
            every { getLocation() } returns URI("file:/tmp")
        }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mockk {
            every { getLocation() } returns URI("file:/tmp")
        }, MediaTypeConstants.TEXT_PLAIN)

        val communicationParameters = mockk<CommunicationParameters> {
            every { getLocation() } returns createTempDir().toURI()
        }

        shouldNotThrowAny {
            FileCommunicationValidatorConverter().validate(listOf(dataDescriptor, dataDescriptor2), communicationParameters)
        }
    }

    @Test
    fun `validate _ communication parameters without location _ should throw CommunicationValidationException`() {
        val data = mockk<Data>()
        val communicationParameters = mockk<CommunicationParameters> {
            every { getLocation() } throws NoSuchElementException()
        }

        shouldThrow<CommunicationValidationException> {
            FileCommunicationValidatorConverter().validate(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)), communicationParameters)
        }.message shouldBe "Communication parameters doesn't contain <location>"
    }

    @Test
    fun `validate _ communication parameters location hasn't file scheme _ should throw CommunicationValidationException`() {
        val data = mockk<Data>()

        val communicationParameters = mockk<CommunicationParameters> {
            every { getLocation() } returns URI("http://noMatter.com")
        }

        shouldThrow<CommunicationValidationException> {
            FileCommunicationValidatorConverter().validate(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)), communicationParameters)
        }.message shouldBe "Communication location <http://noMatter.com> isn't reachable"
    }

    @Test
    fun `validate _ data exists only in memory _ should throw CommunicationValidationException`() {
        val dataDescriptor = DataDescriptor(mockk {
            every { getLocation() } returns URI("file:/tmp")
        }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mockk {
            every { getLocation() } throws UnsupportedOperationException()
        }, MediaTypeConstants.TEXT_PLAIN)

        val communicationParameters = mockk<CommunicationParameters> {
            every { getLocation() } returns createTempDir().toURI()
        }

        shouldThrow<CommunicationValidationException> {
            FileCommunicationValidatorConverter().validate(listOf(dataDescriptor, dataDescriptor2), communicationParameters)
        }.message shouldBe "One of data exists only in memory but should be file"
    }

    @Test
    fun `validate _ data location hasn't file scheme _ should throw CommunicationException`() {
        val communicationParameters = mockk<CommunicationParameters> {
            every { getLocation() } returns createTempDir().toURI()
        }

        val dataDescriptor = DataDescriptor(mockk {
            every { getLocation() } returns URI("file:/tmp")
        }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mockk {
            every { getLocation() } returns URI("http://noMatter.com")
        }, MediaTypeConstants.TEXT_PLAIN)

        shouldThrow<CommunicationValidationException> {
            FileCommunicationValidatorConverter().validate(listOf(dataDescriptor, dataDescriptor2), communicationParameters)
        }.message shouldBe "Data location <http://noMatter.com> hasn't <file> scheme"
    }

    @Test
    fun `validate _ data isn't accessible _ should throw CommunicationValidationException`() {
        val communicationParameters = mockk<CommunicationParameters> {
            every { getLocation() } returns createTempDir().toURI()
        }

        val dataDescriptor = DataDescriptor(mockk {
            every { getLocation() } returns URI("file:/tmp")
        }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mockk {
            every { getLocation() } returns URI("file:/tmp/sub")
            every { isAvailable() } throws DataAccessibilityException("")
        }, MediaTypeConstants.TEXT_PLAIN)

        shouldThrow<CommunicationValidationException> {
            FileCommunicationValidatorConverter().validate(listOf(dataDescriptor, dataDescriptor2), communicationParameters)
        }.message shouldBe "Data (<file:/tmp/sub>) isn't available"
    }
}