package pl.beone.promena.core.external.akka.transformer.config

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

class DefaultTransformersCreatorTest {

    @Test
    fun create() {
        val transformer = mock<Transformer>()
        val transformer2 = mock<Transformer>()
        val transformer3 = mock<Transformer>()

        val transformerConfig = mock<TransformerConfig> {
            on { getTransformationId(transformer) } doReturn "transformer"
            on { getTransformationId(transformer2) } doReturn "transformer"
            on { getTransformationId(transformer3) } doReturn "transformer2"

            on { getActors(transformer) } doReturn 1
            on { getActors(transformer2) } doReturn 2
            on { getActors(transformer3) } doReturn 3

            on { getPriority(transformer) } doReturn 3
            on { getPriority(transformer2) } doReturn 2
            on { getPriority(transformer3) } doReturn 1
        }

        val internalCommunicationConverter = mock<InternalCommunicationConverter>()

        val transformerActorRefWithId =
                ActorRefWithId(mock(), "transformer")
        val transformer2ActorRefWithId =
                ActorRefWithId(mock(), "transformer2")
        val actorCreator = mock<ActorCreator> {
            on { create(eq("transformer"), any(), eq(2)) } doReturn transformerActorRefWithId
            on { create(eq("transformer2"), any(), eq(3)) } doReturn transformer2ActorRefWithId
        }

        val actorRefWithTransformerIdList = DefaultTransformersCreator(
                transformerConfig,
                internalCommunicationConverter,
                actorCreator)
                .create(listOf(transformer,
                               transformer2,
                               transformer3))

        assertThat(actorRefWithTransformerIdList.map { it.id }).containsExactly("transformer", "transformer2")
    }

}