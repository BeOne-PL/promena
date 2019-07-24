package pl.beone.promena.core.external.akka.transformer.config

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithTransformerId
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class AkkaGroupedByNameTransformersCreatorTest {

    @Test
    fun create() {
        val transformer = mockk<Transformer>()
        val transformer2 = mockk<Transformer>()
        val transformer3 = mockk<Transformer>()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformerId(transformer) } returns "transformer".toTransformerId()
            every { getTransformerId(transformer2) } returns ("transformer" to "sub").toTransformerId()
            every { getTransformerId(transformer3) } returns "transformer2".toTransformerId()

            every { getActors(transformer) } returns 1
            every { getActors(transformer2) } returns 2
            every { getActors(transformer3) } returns 3

            every { getPriority(transformer) } returns 3
            every { getPriority(transformer2) } returns 2
            every { getPriority(transformer3) } returns 1
        }

        val internalCommunicationConverter = mockk<InternalCommunicationConverter>()

        val transformerActorRefWithId = ActorRefWithTransformerId(mockk(), "transformer".toTransformerId())
        val transformer2ActorRefWithId = ActorRefWithTransformerId(mockk(), "transformer2".toTransformerId())
        val actorCreator = mockk<ActorCreator> {
            every { create("transformer", any(), 2) } returns transformerActorRefWithId
            every { create("transformer2", any(), 3) } returns transformer2ActorRefWithId
        }

        AkkaGroupedByNameTransformersCreator(transformerConfig, internalCommunicationConverter, actorCreator)
                .create(listOf(transformer, transformer2, transformer3))
                .map { it.transformerId } shouldBe
                listOf("transformer".toTransformerId(), "transformer2".toTransformerId())
    }

}