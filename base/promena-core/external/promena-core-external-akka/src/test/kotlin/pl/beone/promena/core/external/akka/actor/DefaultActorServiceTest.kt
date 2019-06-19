package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException

class DefaultActorServiceTest {

    @Test
    fun `getTransformationActor and getSerializerActor`() {
        val emptyActorWithTransformerId = ActorRefWithId(mockk(), "emptyTransformer")
        val serializerActorRef = mockk<ActorRef>()

        val actorService = DefaultActorService(listOf(emptyActorWithTransformerId), serializerActorRef)

        actorService.getTransformationActor("emptyTransformer") shouldBeSameInstanceAs emptyActorWithTransformerId.ref

        shouldThrow<TransformerNotFoundException> { actorService.getTransformationActor("absent") }
                .message shouldBe "There is no <absent> transformer"

        actorService.getSerializerActor() shouldBe actorService.getSerializerActor()
    }
}