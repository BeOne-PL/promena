package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException

class DefaultActorServiceTest {

    @Test
    fun `getTransformationActor and getSerializerActor`() {
        val emptyActorWithTransformerId =
                ActorRefWithId(mock(), "emptyTransformer")
        val serializerActorRef = mock<ActorRef>()

        val actorService =
                DefaultActorService(listOf(emptyActorWithTransformerId),
                                                                              serializerActorRef)

        assertThat(actorService.getTransformationActor("emptyTransformer")).isSameAs(emptyActorWithTransformerId.ref)

        assertThatThrownBy { actorService.getTransformationActor("absent") }
                .isExactlyInstanceOf(TransformerNotFoundException::class.java)
                .hasMessage("There is no <absent> transformer")

        assertThat(actorService.getSerializerActor()).isSameAs(serializerActorRef)
    }
}