package pl.beone.promena.core.external.akka.transformer.config

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

class DefaultTransformersCreatorTest {

    @Test
    fun create() {
        val transformer = mockk<Transformer>()
        val transformer2 = mockk<Transformer>()
        val transformer3 = mockk<Transformer>()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformationId(transformer) } returns "transformer"
            every { getTransformationId(transformer2) } returns "transformer"
            every { getTransformationId(transformer3) } returns "transformer2"

            every { getActors(transformer) } returns 1
            every { getActors(transformer2) } returns 2
            every { getActors(transformer3) } returns 3

            every { getPriority(transformer) } returns 3
            every { getPriority(transformer2) } returns 2
            every { getPriority(transformer3) } returns 1
        }

        val internalCommunicationConverter = mockk<InternalCommunicationConverter>()

        val transformerActorRefWithId = ActorRefWithId(mockk(), "transformer")
        val transformer2ActorRefWithId = ActorRefWithId(mockk(), "transformer2")
        val actorCreator = mockk<ActorCreator> {
            every { create("transformer", any(), 2) } returns transformerActorRefWithId
            every { create("transformer2", any(), 3) } returns transformer2ActorRefWithId
        }

        DefaultTransformersCreator(transformerConfig, internalCommunicationConverter, actorCreator)
                .create(listOf(transformer, transformer2, transformer3))
                .map { it.id } shouldBe listOf("transformer", "transformer2")
    }

}