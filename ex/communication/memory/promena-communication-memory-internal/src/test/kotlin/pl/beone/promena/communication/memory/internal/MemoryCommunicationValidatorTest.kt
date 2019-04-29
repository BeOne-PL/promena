package pl.beone.promena.communication.memory.internal

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

class MemoryCommunicationValidatorTest {

    companion object {
        private val validator = MemoryCommunicationValidatorConverter()
    }

    @Test
    fun validate() {
        val dataDescriptor = DataDescriptor(mock { on { getLocation() } doThrow UnsupportedOperationException() }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mock { on { getLocation() } doThrow UnsupportedOperationException() }, MediaTypeConstants.TEXT_PLAIN)

        validator.validate(listOf(dataDescriptor, dataDescriptor2),
                           mock())
    }

    @Test
    fun `validate _ has location _ should throw CommunicationValidationException`() {
        val data = mock<Data> {
            on { getLocation() } doReturn URI("file:/tmp")
        }

        assertThatThrownBy {
            validator.validate(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)),
                               mock())
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("Data has location <file:/tmp> but shouldn't")
    }

    @Test
    fun `validate _ data isn't available _ should throw CommunicationValidationException`() {
        val data = mock<Data> {
            on { getLocation() } doThrow UnsupportedOperationException()
            on { isAvailable() } doThrow DataAccessibilityException("")
        }

        assertThatThrownBy {
            validator.validate(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)),
                               mock())
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("One of data isn't available")
    }


}