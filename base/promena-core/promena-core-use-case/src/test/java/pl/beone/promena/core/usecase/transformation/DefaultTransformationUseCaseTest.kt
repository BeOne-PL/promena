package pl.beone.promena.core.usecase.transformation

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerException
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

class DefaultTransformationUseCaseTest {

    companion object {
        private val targetMediaType = MediaTypeConstants.APPLICATION_PDF
        private val parameters = mockk<Parameters>()

        private val dataDescriptors = listOf(DataDescriptor(mockk(), MediaTypeConstants.TEXT_PLAIN))
        private val transformationDescriptor = TransformationDescriptor(dataDescriptors, targetMediaType, parameters)
        private val transformedDataDescriptors = listOf(TransformedDataDescriptor(mockk(), mockk()))
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.core.usecase.transformation.DefaultTransformationUseCase") as Logger).level = Level.DEBUG
    }

    @Test
    fun transform() {
        val externalCommunicationParameters = mockk<CommunicationParameters> {
            every { getId() } returns "memory"
        }
        val internalCommunicationParameters = mockk<CommunicationParameters> {
            every { getId() } returns "memory"
        }

        val incomingCommunicationConverter = mockk<IncomingExternalCommunicationConverter> {
            every { convert(dataDescriptors, externalCommunicationParameters, internalCommunicationParameters) } returns dataDescriptors
        }
        val outgoingCommunicationConverter = mockk<OutgoingExternalCommunicationConverter> {
            every {
                convert(transformedDataDescriptors,
                        externalCommunicationParameters,
                        internalCommunicationParameters)
            } returns transformedDataDescriptors
        }
        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication("memory") } returns
                    ExternalCommunication("memory", incomingCommunicationConverter, outgoingCommunicationConverter)
        }

        val transformerService = mockk<TransformerService> {
            every { transform("default", dataDescriptors, targetMediaType, parameters) } returns transformedDataDescriptors
        }

        DefaultTransformationUseCase(externalCommunicationManager, internalCommunicationParameters, transformerService)
                .transform("default", transformationDescriptor, externalCommunicationParameters) shouldBe transformedDataDescriptors
    }

    @Test
    fun `transform _ should throw ExternalCommunicationManagerException`() {
        val externalCommunicationParameters = mockk<CommunicationParameters> {
            every { getId() } returns "absent"
        }
        val internalCommunicationParameters = mockk<CommunicationParameters>()

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication("absent") } throws ExternalCommunicationManagerException("Exception")
        }

        val transformerService = mockk<TransformerService>()

        shouldThrowExactly<ExternalCommunicationManagerException> {
            DefaultTransformationUseCase(externalCommunicationManager, internalCommunicationParameters, transformerService)
                    .transform("default", transformationDescriptor, externalCommunicationParameters)
        }.message shouldBe "Exception"
    }
}