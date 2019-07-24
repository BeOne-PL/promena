package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithTransformerId
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class AkkaGroupedByNameActorServiceTest {

    @Test
    fun `getTransformerActor and getSerializerActor`() {
        val emptyActorWithTransformerId = ActorRefWithTransformerId(mockk(), "emptyTransformer".toTransformerId())
        val serializerActorRef = mockk<ActorRef>()

        val actorService = AkkaGroupedByNameActorService(listOf(emptyActorWithTransformerId), serializerActorRef)

        actorService.getTransformerActor("emptyTransformer".toTransformerId()) shouldBeSameInstanceAs
                emptyActorWithTransformerId.ref

        actorService.getTransformerActor(("emptyTransformer" to "subEmptyTransformer").toTransformerId()) shouldBeSameInstanceAs
                emptyActorWithTransformerId.ref

        shouldThrow<TransformerNotFoundException> { actorService.getTransformerActor("absent".toTransformerId()) }.message shouldBe
                "There is no <absent> transformer"

        shouldThrow<TransformerNotFoundException> { actorService.getTransformerActor(("absent" to "subAbsent").toTransformerId()) }.message shouldBe
                "There is no <absent> transformer"

        actorService.getSerializerActor() shouldBe
                serializerActorRef
    }
}