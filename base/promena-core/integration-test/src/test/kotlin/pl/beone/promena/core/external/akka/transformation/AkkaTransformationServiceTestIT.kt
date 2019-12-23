package pl.beone.promena.core.external.akka.transformation

import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.AskTimeoutException
import akka.stream.AbruptStageTerminationException
import akka.stream.ActorMaterializer
import akka.stream.BufferOverflowException
import akka.testkit.javadsl.TestKit
import com.typesafe.config.ConfigFactory
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.external.akka.actor.GroupedByNameTransformerActorGetter
import pl.beone.promena.core.external.akka.actor.transformer.GroupedByNameTransformerActor
import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.core.external.akka.extension.toCorrectActorName
import pl.beone.promena.core.external.akka.transformation.converter.MirrorInternalCommunicationConverter
import pl.beone.promena.core.external.akka.transformation.converter.NothingInternalCommunicationCleaner
import pl.beone.promena.core.external.akka.transformation.transformer.FromTextToXmlAppenderTransformer
import pl.beone.promena.core.external.akka.transformation.transformer.TextAppenderTransformer
import pl.beone.promena.core.external.akka.transformation.transformer.TimeoutTransformer
import pl.beone.promena.core.external.akka.transformation.transformer.UselessTextAppenderTransformer
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_EPUB_ZIP
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.contract.transformer.toTransformerId
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.addTimeout
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.time.Duration

class AkkaTransformationServiceTestIT {

    companion object {
        private val internalCommunicationConverter = MirrorInternalCommunicationConverter
        private val internalCommunicationCleaner = NothingInternalCommunicationCleaner

        private const val textAppenderTransformerName = "text appender"
        private const val kotlinTextAppenderTransformerSubName = "kotlin"
        private const val javaTextAppenderTransformerSubName = "java"
        private const val uselessTextAppenderTransformerSubName = "Kotlin useless"
        private const val fromTextToXmlAppenderTransformerName = "from&text*to(xml)appender"
        private const val fromTextToXmlAppenderTransformerSubName = "kotlin"
        private const val timeoutTransformerName = "timeout"
        private const val timeoutTransformerSubName = "kotlin"
    }

    private lateinit var actorSystem: ActorSystem

    @BeforeEach
    fun setUp() {
        actorSystem = ActorSystem.create("Promena", ConfigFactory.load("resource-test.conf"))
    }

    @AfterEach
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun `transform _ single transformation`() {
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true))

        val transformation =
            singleTransformation(textAppenderTransformerName, TEXT_PLAIN, emptyParameters() + ("append" to "$"))

        val transformerService = prepareTransformationService()

        with(transformerService.transform(transformation, dataDescriptor).descriptors) {
            this shouldHaveSize 1

            with(this[0]) {
                data.getString() shouldBe "test$"
                metadata shouldBe (emptyMetadata() + ("begin" to true) + ("text appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ single transformation with sub name`() {
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true))

        val transformation = singleTransformation(
            (textAppenderTransformerName to javaTextAppenderTransformerSubName).toTransformerId(),
            TEXT_PLAIN,
            emptyParameters() + ("append" to "$")
        )

        val transformerService = prepareTransformationService()

        with(transformerService.transform(transformation, dataDescriptor).descriptors) {
            this shouldHaveSize 1

            with(this[0]) {
                data.getString() shouldBe "test$"
                metadata shouldBe (emptyMetadata() + ("begin" to true) + ("java-text appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ composite transformation`() {
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true)) +
                singleDataDescriptor("test2".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin2" to true))

        val transformation =
            singleTransformation(textAppenderTransformerName, TEXT_PLAIN, emptyParameters() + ("append" to "$")) next
                    singleTransformation(fromTextToXmlAppenderTransformerName, TEXT_XML, emptyParameters() + ("tag" to "root"))

        val transformerService = prepareTransformationService()

        with(transformerService.transform(transformation, dataDescriptor).descriptors) {
            this shouldHaveSize 2

            with(this[0]) {
                data.getString() shouldBe "<root>test$</root>"
                metadata shouldBe
                        (emptyMetadata() + ("begin" to true) + ("text appender-transformer" to true) + ("from&text*to(xml)appender-transformer" to true))
            }

            with(this[1]) {
                data.getString() shouldBe "<root>test2$</root>"
                metadata shouldBe
                        (emptyMetadata() + ("begin2" to true) + ("text appender-transformer" to true) + ("from&text*to(xml)appender-transformer" to true))
            }
        }
    }

    @Test
    fun `transform _ no transformer with given id _ should throw TransformationException - created from TransformerNotFoundException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation("absentTransformer", TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService()

        with(shouldThrowExactly<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }) {
            message shouldBe "There is no <absentTransformer> transformer"
            causeClass shouldBe TransformerNotFoundException::class.java
        }
    }

    @Test
    fun `transform _ target media type that isn't supported by transformer _ should throw TransformationException - created from TransformationNotSupportedException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerName, APPLICATION_EPUB_ZIP, emptyParameters())

        val transformerService = prepareTransformationService()

        with(shouldThrowExactly<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }) {
            with(message!!.split("\n")) {
                this[0] shouldBe "Transformation isn't supported | There is no transformer in group <text appender> that support given transformation"
                this[1] shouldBe "> pl.beone.promena.core.external.akka.transformation.transformer.TextAppenderTransformer(text appender, kotlin): Only transformation from text/plain to text/plain is supported"
                this[2] shouldBe "> pl.beone.promena.core.external.akka.transformation.transformer.UselessTextAppenderTransformer(text appender, Kotlin useless): I can't transform nothing. I'm useless"
                this[3] shouldBe "> pl.beone.promena.core.external.akka.transformation.JavaTextAppenderTransformer(text appender, java): Only transformation from text/plain to text/plain is supported"
            }
            causeClass shouldBe TransformationNotSupportedException::class.java
        }
    }

    @Test
    fun `transform _ target media type that isn't supported by transformer and detailed transformer id _ should throw TransformationException - created from TransformationNotSupportedException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerName, "kotlin", APPLICATION_EPUB_ZIP, emptyParameters())

        val transformerService = prepareTransformationService()

        with(shouldThrowExactly<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }) {
            message shouldBe "Transformation isn't supported | Transformer pl.beone.promena.core.external.akka.transformation.transformer.TextAppenderTransformer(text appender, kotlin) doesn't support given transformation: Only transformation from text/plain to text/plain is supported"
            causeClass shouldBe TransformationNotSupportedException::class.java
        }
    }

    @Test
    fun `transform _ transformer timeout has been reached _ should throw TransformationException - created from TransformerTimeoutException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("begin" to true))

        val transformation = singleTransformation(textAppenderTransformerName, TEXT_PLAIN, emptyParameters() + ("append" to "$")) next
                singleTransformation(timeoutTransformerName, TEXT_PLAIN, emptyParameters() addTimeout Duration.ofMillis(1))

        val transformerService = prepareTransformationService()

        with(shouldThrowExactly<TransformationException> {
            transformerService.transform(transformation, dataDescriptor)
        }) {
            message shouldBe "Transformer <timeout> timeout <1ms> has been reached"
            causeClass shouldBe TransformerTimeoutException::class.java
        }
    }

    @Test
    fun `transform _ akka ask timeout has been reached _ should throw TransformationException - created from AskTimeoutException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerName, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws AskTimeoutException("")
        })

        with(shouldThrowExactly<TransformationTerminationException> {
            transformerService.transform(transformation, dataDescriptor)
        }) {
            message shouldBe "Transformation timeout has been reached. It's highly likely that Promena performing transformation has been shutdown"
            causeClass shouldBe null
        }
    }

    @Test
    fun `transform _ unexpected abrupt stage exception (generally caused by closing server suddenly) _ should throw TransformationException - created from AbruptStageTerminationException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerName, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws AbruptStageTerminationException(null)
        })

        with(shouldThrowExactly<TransformationTerminationException> {
            transformerService.transform(transformation, dataDescriptor)
        }) {
            message shouldBe "Transformation has been abruptly terminated"
            causeClass shouldBe null
        }
    }

    @Test
    fun `transform _ unknown exception _ should throw TransformationException`() {
        val dataDescriptor = singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())

        val transformation = singleTransformation(textAppenderTransformerName, TEXT_PLAIN, emptyParameters())

        val transformerService = prepareTransformationService(mockk {
            every { materialize<Any>(any()) } throws BufferOverflowException("Bytes... Bytes everywhere")
        })

        shouldThrowExactly<BufferOverflowException> {
            transformerService.transform(transformation, dataDescriptor)
        }.message shouldBe "Bytes... Bytes everywhere"
    }

    private fun prepareTransformationService(actorMaterializer: ActorMaterializer = ActorMaterializer.create(actorSystem)): AkkaTransformationService {
        val textAppenderTransformerId = (textAppenderTransformerName to kotlinTextAppenderTransformerSubName).toTransformerId()
        val javaTextAppenderTransformerId = (textAppenderTransformerName to javaTextAppenderTransformerSubName).toTransformerId()
        val uselessTextAppenderTransformerId = (textAppenderTransformerName to uselessTextAppenderTransformerSubName).toTransformerId()
        val fromTextToXmlAppenderTransformerId = (fromTextToXmlAppenderTransformerName to fromTextToXmlAppenderTransformerSubName).toTransformerId()
        val timeoutTransformerId = (timeoutTransformerName to timeoutTransformerSubName).toTransformerId()

        val textAppenderTransformerActorRef = actorSystem.actorOf(
            Props.create(GroupedByNameTransformerActor::class.java) {
                GroupedByNameTransformerActor(
                    textAppenderTransformerName,
                    listOf(
                        TransformerDescriptor(textAppenderTransformerId, TextAppenderTransformer),
                        TransformerDescriptor(uselessTextAppenderTransformerId, UselessTextAppenderTransformer),
                        TransformerDescriptor(javaTextAppenderTransformerId, JavaTextAppenderTransformer())
                    ),
                    internalCommunicationConverter,
                    internalCommunicationCleaner
                )
            }, textAppenderTransformerName.toCorrectActorName()
        )

        val fromTextToXmlAppenderTransformerActorRef = actorSystem.actorOf(
            Props.create(GroupedByNameTransformerActor::class.java) {
                GroupedByNameTransformerActor(
                    fromTextToXmlAppenderTransformerName,
                    listOf(TransformerDescriptor(fromTextToXmlAppenderTransformerId, FromTextToXmlAppenderTransformer)),
                    internalCommunicationConverter,
                    internalCommunicationCleaner
                )
            }, fromTextToXmlAppenderTransformerName.toCorrectActorName()
        )

        val timeoutTransformerActorRef = actorSystem.actorOf(
            Props.create(GroupedByNameTransformerActor::class.java) {
                GroupedByNameTransformerActor(
                    timeoutTransformerName,
                    listOf(TransformerDescriptor(timeoutTransformerId, TimeoutTransformer)),
                    internalCommunicationConverter,
                    internalCommunicationCleaner
                )
            }, timeoutTransformerName.toCorrectActorName()
        )

        val actorService = GroupedByNameTransformerActorGetter(
            listOf(
                TransformerActorDescriptor(textAppenderTransformerId, textAppenderTransformerActorRef, 1),
                TransformerActorDescriptor(javaTextAppenderTransformerId, textAppenderTransformerActorRef, 1),
                TransformerActorDescriptor(uselessTextAppenderTransformerId, textAppenderTransformerActorRef, 1),
                TransformerActorDescriptor(fromTextToXmlAppenderTransformerId, fromTextToXmlAppenderTransformerActorRef, 1),
                TransformerActorDescriptor(timeoutTransformerId, timeoutTransformerActorRef, 1)
            )
        )

        return AkkaTransformationService(Duration.ofSeconds(3), Duration.ofSeconds(3), actorMaterializer, actorService)
    }

    private fun Data.getString(): String =
        String(getBytes())
}
