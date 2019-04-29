package pl.beone.promena.communication.file.internal

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationValidationException
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import java.net.URI

class FileCommunicationValidatorTest {

    companion object {
        private val validator = FileCommunicationValidatorConverter()
    }

    @Test
    fun validate() {
        val communicationParameters = mock<CommunicationParameters> {
            on { getLocation() } doReturn createTempDir().toURI()
        }

        val dataDescriptor = DataDescriptor(mock { on { getLocation() } doReturn URI("file:/tmp") }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mock { on { getLocation() } doReturn URI("file:/tmp") }, MediaTypeConstants.TEXT_PLAIN)

        validator.validate(listOf(dataDescriptor, dataDescriptor2),
                           communicationParameters)
    }

    @Test
    fun `validate _ communication parameters without location _ should throw CommunicationValidationException`() {
        val communicationParameters = mock<CommunicationParameters> {
            on { getLocation() } doThrow NoSuchElementException()
        }

        assertThatThrownBy {
            validator.validate(listOf(DataDescriptor(mock(), MediaTypeConstants.TEXT_PLAIN)),
                               communicationParameters)
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("Communication parameters doesn't contain <location>")
    }

    @Test
    fun `validate _ communication parameters location hasn't file scheme _ should throw CommunicationValidationException`() {
        val communicationParameters = mock<CommunicationParameters> {
            on { getLocation() } doReturn URI("http://noMatter.com")
        }

        assertThatThrownBy {
            validator.validate(listOf(DataDescriptor(mock(), MediaTypeConstants.TEXT_PLAIN)),
                               communicationParameters)
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("Communication location <http://noMatter.com> isn't reachable")
    }

    @Test
    fun `validate _ data exists only in memory _ should throw CommunicationValidationException`() {
        val communicationParameters = mock<CommunicationParameters> {
            on { getLocation() } doReturn createTempDir().toURI()
        }

        val dataDescriptor = DataDescriptor(mock { on { getLocation() } doReturn URI("file:/tmp") }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mock { on { getLocation() } doThrow UnsupportedOperationException() }, MediaTypeConstants.TEXT_PLAIN)

        assertThatThrownBy {
            validator.validate(listOf(dataDescriptor, dataDescriptor2),
                               communicationParameters)
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("One of data exists only in memory but should be file")
    }

    @Test
    fun `validate _ data location hasn't file scheme _ should throw CommunicationException`() {
        val communicationParameters = mock<CommunicationParameters> {
            on { getLocation() } doReturn createTempDir().toURI()
        }

        val dataDescriptor = DataDescriptor(mock { on { getLocation() } doReturn URI("file:/tmp") }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mock { on { getLocation() } doReturn URI("http://noMatter.com") }, MediaTypeConstants.TEXT_PLAIN)

        assertThatThrownBy {
            validator.validate(listOf(dataDescriptor, dataDescriptor2),
                               communicationParameters)
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("Data location <http://noMatter.com> hasn't <file> scheme")
    }

    @Test
    fun `validate _ data isn't accessible _ should throw CommunicationValidationException`() {
        val communicationParameters = mock<CommunicationParameters> {
            on { getLocation() } doReturn createTempDir().toURI()
        }

        val dataDescriptor = DataDescriptor(mock { on { getLocation() } doReturn URI("file:/tmp") }, MediaTypeConstants.TEXT_PLAIN)
        val dataDescriptor2 = DataDescriptor(mock {
            on { getLocation() } doReturn URI("file:/tmp/sub")
            on { isAvailable() } doThrow DataAccessibilityException("")
        }, MediaTypeConstants.TEXT_PLAIN)

        assertThatThrownBy {
            validator.validate(listOf(dataDescriptor, dataDescriptor2),
                               communicationParameters)
        }
                .isExactlyInstanceOf(CommunicationValidationException::class.java)
                .hasMessage("Data (<file:/tmp/sub>) isn't available")
    }
}