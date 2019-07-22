package pl.beone.promena.core.external.akka.transformation

import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.AskTimeoutException
import akka.stream.AbruptStageTerminationException
import akka.stream.ActorMaterializer
import akka.stream.BufferOverflowException
import akka.testkit.javadsl.TestKit
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.external.akka.actor.DefaultActorService
import pl.beone.promena.core.external.akka.actor.transformer.TransformerActor
import pl.beone.promena.core.external.akka.transformation.converter.MirrorInternalCommunicationConverter
import pl.beone.promena.core.external.akka.transformation.transformer.FromTextToXmlAppenderTransformer
import pl.beone.promena.core.external.akka.transformation.transformer.TextAppenderTransformer
import pl.beone.promena.core.external.akka.transformation.transformer.TimeoutTransformer
import pl.beone.promena.core.external.akka.transformation.transformer.UselessTextAppenderTransformer
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_EPUB_ZIP
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.addTimeout
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Duration

class AkkaTransformationServiceTest {

    companion object {
        private val internalCommunicationConverter = MirrorInternalCommunicationConverter()

        private const val textAppenderTransformerId = "text-appender-transformer"
        private const val fromTextToXmlAppenderTransformerId = "from-text-to-xml-appender-transformer"
        private const val timeoutTransformerId = "timeout-transformer"
    }

    private lateinit var actorSystem: ActorSystem

    @Before
    fun setUp() {
        actorSystem = ActorSystem.create()

        (LoggerFactory.getLogger("pl.beone.promena.core.external.akka.transformer.AkkaTransformerService") as Logger).level = Level.DEBUG
    }

    @After
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun `transform _ single transformation`() {
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true))

        val transformation =
            singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters() + ("append" to "$"))

        val transformerService = prepareTransformationService()

        transformerService.transform(transformation, dataDescriptor).descriptors.let { transformedDataDescriptor ->
            transformedDataDescriptor shouldHaveSize 1

            transformedDataDescriptor[0].let { singleTransformedDataDescriptor ->
                singleTransformedDataDescriptor.data.getString() shouldBe "test$"
                singleTransformedDataDescriptor.metadata shouldBe (emptyMetadata() + ("begin" to true) + ("text-appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ composite transformation`() {
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true)) +
                             singleDataDescriptor("test2".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin2" to true))


        val transformation =
            singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters() + ("append" to "$")) next
                    singleTransformation(fromTextToXmlAppenderTransformerId, TEXT_XML, emptyParameters() + ("tag" to "root"))

        val transformerService = prepareTransformationService()

        transformerService.transform(transformation, dataDescriptor).descriptors.let { transformedDataDescriptor ->
            transformedDataDescriptor shouldHaveSize 2

            transformedDataDescriptor[0].let { singleTransformedDataDescriptor ->
                singleTransformedDataDescriptor.data.getString() shouldBe "<root>test$</root>"
                singleTransformedDataDescriptor.metadata shouldBe
                        (emptyMetadata() + ("begin" to true) + ("text-appender-transformer" to true) + ("from-text-to-xml-appender-transformer" to true))
            }

            transformedDataDescriptor[1].let { singleTransformedDataDescriptor ->
                singleTransformedDataDescriptor.data.getString() shouldBe "<root>test2$</root>"
                singleTransformedDataDescriptor.metadata shouldBe
                        (emptyMetadata() + ("begin2" to true) + ("text-appender-transformer" to true) + ("from-text-to-xml-appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ no transformer with given id _ should throw TransformationException(created from TransformerNotFoundException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation("absentTransformer", TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService()

        shouldThrow<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }.apply {
            this.transformation shouldBe transformation
            this.message shouldBe "Couldn't perform the transformation | There is no <absentTransformer> transformer"
            this.getStringStackTrace() shouldContain "TransformerNotFoundException"
        }
    }

    @Test
    fun `transform _ target media type that isn't supported by transformer _ should throw TransformationException (created from TransformersCouldNotTransformException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerId, APPLICATION_EPUB_ZIP, emptyParameters())

        val transformerService = prepareTransformationService()

        shouldThrow<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }.apply {
            this.transformation shouldBe transformation
            this.message shouldBe "Couldn't perform the transformation | There is no <text-appender-transformer> transformer that can transform data descriptors [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>] using <MediaType(mimeType=application/epub+zip, charset=UTF-8), MapParameters(parameters={})>: [<pl.beone.promena.core.external.akka.transformation.transformer.TextAppenderTransformer, Only a transformation from text/plain to text/plain is supported>, <pl.beone.promena.core.external.akka.transformation.transformer.UselessTextAppenderTransformer, I can't transform nothing. I'm useless>]"
            this.getStringStackTrace() shouldContain "TransformersCouldNotTransformException"
        }
    }

    @Test
    fun `transform _ transformer timeout has been reached _ should throw TransformationException (created from TransformerTimeoutException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true))

        val transformation = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters() + ("append" to "$")) next
                singleTransformation(timeoutTransformerId, TEXT_PLAIN, emptyParameters() addTimeout Duration.ofMillis(1))

        val transformerService = prepareTransformationService()

        shouldThrow<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }.apply {
            this.transformation shouldBe transformation
            this.message shouldBe "Couldn't perform the transformation | Couldn't transform because the transformer <timeout-transformer> timeout <PT0.001S> has been reached"
            this.getStringStackTrace() shouldContain "TransformerTimeoutException"
        }
    }

    @Test
    fun `transform _ akka ask timeout has been reached _ should throw TransformationException (created from AskTimeoutException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws AskTimeoutException("")
        })

        shouldThrow<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }.apply {
            this.transformation shouldBe transformation
            this.message shouldBe "Couldn't perform the transformation because timeout has been reached"
            this.getStringStackTrace() shouldContain "AskTimeoutException"
        }
    }

    @Test
    fun `transform _ unexpected abrupt stage exception (generally caused by closing server suddenly) _ should throw TransformationException (created from AbruptStageTerminationException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws AbruptStageTerminationException(null)
        })

        shouldThrow<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }.apply {
            this.transformation shouldBe transformation
            this.message shouldBe "Could not perform the transformation because it was abruptly terminated"
            this.getStringStackTrace() shouldContain "AbruptStageTerminationException"
        }
    }

    @Test
    fun `transform _ unknown exception _ should throw TransformationException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws BufferOverflowException("Bytes... Bytes everywhere")
        })

        shouldThrow<BufferOverflowException> {
            transformerService.transform(transformation, dataDescriptor)
        }.apply {
            this.message shouldBe "Bytes... Bytes everywhere"
        }
    }

    private fun prepareTransformationService(actorMaterializer: ActorMaterializer = ActorMaterializer.create(actorSystem)): AkkaTransformationService {
        val textAppenderTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java) {
                    TransformerActor(textAppenderTransformerId,
                                     listOf(TextAppenderTransformer(), UselessTextAppenderTransformer()),
                                     internalCommunicationConverter)
                }, textAppenderTransformerId
        )

        val fromTextToXmlAppenderTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java) {
                    TransformerActor(fromTextToXmlAppenderTransformerId, listOf(FromTextToXmlAppenderTransformer()), internalCommunicationConverter)
                }, fromTextToXmlAppenderTransformerId
        )

        val timeoutTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java) {
                    TransformerActor(timeoutTransformerId, listOf(TimeoutTransformer()), internalCommunicationConverter)
                }, timeoutTransformerId
        )

        val actorService = DefaultActorService(listOf(ActorRefWithId(textAppenderTransformerActorRef, textAppenderTransformerId),
                                                      ActorRefWithId(fromTextToXmlAppenderTransformerActorRef, fromTextToXmlAppenderTransformerId),
                                                      ActorRefWithId(timeoutTransformerActorRef, timeoutTransformerId)),
                                               mockk())

        return AkkaTransformationService(actorMaterializer, actorService)
    }

    private fun Data.getString(): String =
        String(getBytes())

    private fun Exception.getStringStackTrace(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        this.printStackTrace(pw)
        return sw.toString()
    }
}
