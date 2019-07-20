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
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_EPUB_ZIP
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import pl.beone.promena.transformer.internal.model.parameters.addTimeout
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

        val transformationFlow =
                singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters() + ("append" to "$"))

        val transformerService = prepareTransformationService()

        transformerService.transform(transformationFlow, dataDescriptor).descriptors.let { transformedDataDescriptors ->
            transformedDataDescriptors shouldHaveSize 1

            transformedDataDescriptors[0].let { transformedDataDescriptor ->
                transformedDataDescriptor.data.getString() shouldBe "test$"
                transformedDataDescriptor.metadata shouldBe (emptyMetadata() + ("begin" to true) + ("text-appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ composite transformation`() {
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true)) +
                singleDataDescriptor("test2".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin2" to true))


        val transformationFlow =
                singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters() + ("append" to "$")) next
                        singleTransformation(fromTextToXmlAppenderTransformerId, TEXT_XML, emptyParameters() + ("tag" to "root"))

        val transformerService = prepareTransformationService()

        transformerService.transform(transformationFlow, dataDescriptor).descriptors.let { transformedDataDescriptors ->
            transformedDataDescriptors shouldHaveSize 2

            transformedDataDescriptors[0].let { transformedDataDescriptor ->
                transformedDataDescriptor.data.getString() shouldBe "<root>test$</root>"
                transformedDataDescriptor.metadata shouldBe
                        (emptyMetadata() + ("begin" to true) + ("text-appender-transformer" to true) + ("from-text-to-xml-appender-transformer" to true))
            }

            transformedDataDescriptors[1].let { transformedDataDescriptor ->
                transformedDataDescriptor.data.getString() shouldBe "<root>test2$</root>"
                transformedDataDescriptor.metadata shouldBe
                        (emptyMetadata() + ("begin2" to true) + ("text-appender-transformer" to true) + ("from-text-to-xml-appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ no transformer with given id _ should throw TransformationException(created from TransformerNotFoundException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformationFlow = singleTransformation("absentTransformer", TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService()

        shouldThrow<TransformationException> {
            transformerService.transform(transformationFlow, dataDescriptor)
        }.apply {
            this.message shouldBe "Couldn't perform the transformation | There is no <absentTransformer> transformer | <Single(id=absentTransformer, targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))> <1 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "TransformerNotFoundException"
        }
    }

    @Test
    fun `transform _ target media type that isn't supported by transformer _ should throw TransformationException (created from TransformerCanNotTransformException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformationFlow = singleTransformation(textAppenderTransformerId, APPLICATION_EPUB_ZIP, emptyParameters())

        val transformerService = prepareTransformationService()

        shouldThrow<TransformationException> {
            transformerService.transform(transformationFlow, dataDescriptor)
        }.apply {
            this.message shouldBe "Couldn't perform the transformation | There is no <text-appender-transformer> transformer that can transform it. The following <1> transformers are available: <pl.beone.promena.core.external.akka.transformation.transformer.TextAppenderTransformer> | <Single(id=text-appender-transformer, targetMediaType=MediaType(mimeType=application/epub+zip, charset=UTF-8), parameters=MapParameters(parameters={}))> <1 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "TransformerCanNotTransformException"
        }
    }

    @Test
    fun `transform _ transformer timeout has been reached _ should throw TransformationException (created from TransformerTimeoutException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true))

        val transformationFlow = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters() + ("append" to "$")) next
                singleTransformation(timeoutTransformerId, TEXT_PLAIN, emptyParameters() addTimeout Duration.ofMillis(1))

        val transformerService = prepareTransformationService()

        shouldThrow<TransformationException> {
            transformerService.transform(transformationFlow, dataDescriptor)
        }.apply {
            this.message shouldBe "Couldn't perform the transformation | Couldn't transform because the transformer <timeout-transformer> timeout <PT0.001S> has been reached | <Composite(transformers=[Single(id=text-appender-transformer, targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={append=$})), Single(id=timeout-transformer, targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={timeout=PT0.001S}))])> <1 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "TransformerTimeoutException"
        }
    }

    @Test
    fun `transform _ akka ask timeout has been reached _ should throw TransformationException (created from AskTimeoutException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformationFlow = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws AskTimeoutException("")
        })

        shouldThrow<TransformationException> {
            transformerService.transform(transformationFlow, dataDescriptor)
        }.apply {
            this.message shouldBe "Couldn't perform the transformation because timeout has been reached | <Single(id=text-appender-transformer, targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))> <1 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "AskTimeoutException"
        }
    }

    @Test
    fun `transform _ unexpected abrupt stage exception (generally caused by closing server suddenly) _ should throw TransformationException (created from AbruptStageTerminationException)`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformationFlow = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws AbruptStageTerminationException(null)
        })

        shouldThrow<TransformationException> {
            transformerService.transform(transformationFlow, dataDescriptor)
        }.apply {
            this.message shouldBe "Couldn't transform because the transformation was abruptly terminated | <Single(id=text-appender-transformer, targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))> <1 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "AbruptStageTerminationException"
        }
    }

    @Test
    fun `transform _ unknown exception _ should throw TransformationException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformationFlow = singleTransformation(textAppenderTransformerId, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws BufferOverflowException("")
        })

        shouldThrow<TransformationException> {
            transformerService.transform(transformationFlow, dataDescriptor)
        }.apply {
            this.message shouldBe "Couldn't transform because a unknown error occurred. Check Promena logs for more details | <Single(id=text-appender-transformer, targetMediaType=MediaType(mimeType=text/plain, charset=UTF-8), parameters=MapParameters(parameters={}))> <1 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "BufferOverflowException"
        }
    }

    private fun prepareTransformationService(actorMaterializer: ActorMaterializer = ActorMaterializer.create(actorSystem)): AkkaTransformationService {
        val textAppenderTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java) {
                    TransformerActor(textAppenderTransformerId, listOf(TextAppenderTransformer()), internalCommunicationConverter)
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
