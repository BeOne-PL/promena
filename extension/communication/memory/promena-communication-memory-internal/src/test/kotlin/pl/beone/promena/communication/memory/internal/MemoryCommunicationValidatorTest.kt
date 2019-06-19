package pl.beone.promena.communication.memory.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

class MemoryCommunicationValidatorTest {

    companion object {
        private val communicationParameters = MapCommunicationParameters.empty()
    }

    @Test
    fun validate() {
        val data = mockk<Data> {
            every { getLocation() } throws UnsupportedOperationException()
            every { isAvailable() } just Runs
        }
        val data2 = mockk<Data> {
            every { getLocation() } throws UnsupportedOperationException()
            every { isAvailable() } just Runs
        }

        val dataDescriptor = DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(data2, MediaTypeConstants.TEXT_PLAIN)

        shouldNotThrowAny {
            MemoryCommunicationValidatorConverter().validate(listOf(dataDescriptor, dataDescriptor2), communicationParameters)
        }
    }

    @Test
    fun `validate _ has location _ should throw CommunicationValidationException`() {
        val data = mockk<Data> {
            every { getLocation() } returns URI("file:/tmp")
        }

        shouldThrow<CommunicationValidationException> {
            MemoryCommunicationValidatorConverter().validate(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)), communicationParameters)
        }.message shouldBe "Data has location <file:/tmp> but shouldn't"
    }

    @Test
    fun `validate _ data isn't available _ should throw CommunicationValidationException`() {
        val data = mockk<Data> {
            every { getLocation() } throws UnsupportedOperationException()
            every { isAvailable() } throws DataAccessibilityException("")
        }

        shouldThrow<CommunicationValidationException> {
            MemoryCommunicationValidatorConverter().validate(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)), communicationParameters)
        }.message shouldBe "One of data isn't available"
    }
}