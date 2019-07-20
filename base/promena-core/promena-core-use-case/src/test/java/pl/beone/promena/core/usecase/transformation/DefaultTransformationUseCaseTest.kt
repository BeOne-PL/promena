package pl.beone.promena.core.usecase.transformation

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.transformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.singleTransformation

class DefaultTransformationUseCaseTest {

    companion object {
        private val targetMediaType = MediaTypeConstants.APPLICATION_PDF
        private val parameters = mockk<Parameters>()

        private val dataDescriptor = singleDataDescriptor(mockk(), TEXT_PLAIN, mockk())
        private val transformationFlow = singleTransformation("test", targetMediaType, parameters)
        private val transformedDataDescriptor = transformedDataDescriptor(mockk(), mockk())
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.core.usecase.transformation.DefaultTransformationUseCase") as Logger).level = Level.DEBUG
    }


    @Test
    fun transform() {
        val externalCommunicationId = "memory"
        val externalCommunicationParameters = mockk<CommunicationParameters> {
            every { getId() } returns externalCommunicationId
        }
        val incomingCommunicationConverter = mockk<IncomingExternalCommunicationConverter> {
            every { convert(dataDescriptor, externalCommunicationParameters) } returns dataDescriptor
        }
        val outgoingCommunicationConverter = mockk<OutgoingExternalCommunicationConverter> {
            every {
                convert(transformedDataDescriptor, externalCommunicationParameters)
            } returns transformedDataDescriptor
        }

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(externalCommunicationId) } returns
                    ExternalCommunication(externalCommunicationId, incomingCommunicationConverter, outgoingCommunicationConverter)
        }

        val transformerService = mockk<TransformationService> {
            every { transform(transformationFlow, dataDescriptor) } returns transformedDataDescriptor
        }

        DefaultTransformationUseCase(externalCommunicationManager, transformerService)
                .transform(transformationFlow, dataDescriptor, externalCommunicationParameters) shouldBe transformedDataDescriptor
    }

    @Test
    fun `transform _ externalCommunicationManager throws ExternalCommunicationManagerException _ should rethrow exception`() {
        val externalCommunicationId = "memory"
        val externalCommunicationParameters = mockk<CommunicationParameters> {
            every { getId() } returns externalCommunicationId
        }

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(externalCommunicationId) } throws
                    ExternalCommunicationManagerException("Exception occurred", RuntimeException("Stack exception"))
        }

        shouldThrow<ExternalCommunicationManagerException> {
            DefaultTransformationUseCase(externalCommunicationManager, mockk())
                    .transform(transformationFlow, dataDescriptor, externalCommunicationParameters)
        }.let {
            it.message shouldBe "Exception occurred"
            it.cause shouldNotBe null
        }
    }

    @Test
    fun `transform _ transformationService throws TransformationException _ should unwrap and rethrow exception`() {
        val externalCommunicationId = "memory"
        val externalCommunicationParameters = mockk<CommunicationParameters> {
            every { getId() } returns externalCommunicationId
        }
        val incomingCommunicationConverter = mockk<IncomingExternalCommunicationConverter> {
            every { convert(dataDescriptor, externalCommunicationParameters) } returns dataDescriptor
        }

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(externalCommunicationId) } returns
                    ExternalCommunication(externalCommunicationId, incomingCommunicationConverter, mockk())
        }

        val transformerService = mockk<TransformationService> {
            every { transform(transformationFlow, dataDescriptor) } throws
                    TransformationException("Exception occurred", RuntimeException("Stack exception"))
        }

        shouldThrow<TransformationException> {
            DefaultTransformationUseCase(externalCommunicationManager, transformerService)
                    .transform(transformationFlow, dataDescriptor, externalCommunicationParameters)
        }.let {
            it.message shouldBe "Exception occurred"
            it.cause shouldBe null
        }
    }
}