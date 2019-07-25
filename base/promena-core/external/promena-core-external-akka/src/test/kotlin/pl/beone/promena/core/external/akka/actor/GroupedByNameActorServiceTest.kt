package pl.beone.promena.core.external.akka.actor

import akka.actor.ActorRef
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class GroupedByNameActorServiceTest {

    @Test
    fun getTransformerActor() {
        val veryEmptyTransformerId = ("empty" to "very").toTransformerId()
        val veryEmptyTransformerActorDescriptor = TransformerActorDescriptor(veryEmptyTransformerId, mockk())

        val actorService = GroupedByNameActorService(listOf(veryEmptyTransformerActorDescriptor),
                                                     mockk())

        actorService.getTransformerActor("empty".toTransformerId()) shouldBeSameInstanceAs
                veryEmptyTransformerActorDescriptor.actorRef

        actorService.getTransformerActor(veryEmptyTransformerId) shouldBeSameInstanceAs
                veryEmptyTransformerActorDescriptor.actorRef

        shouldThrow<TransformerNotFoundException> { actorService.getTransformerActor("absent".toTransformerId()) }.message shouldBe
                "There is no <absent> transformer"

        shouldThrow<TransformerNotFoundException> { actorService.getTransformerActor(("empty" to "not-much").toTransformerId()) }.message shouldBe
                "There is no <empty, not-much> transformer"

        shouldThrow<TransformerNotFoundException> { actorService.getTransformerActor("absent".toTransformerId()) }.message shouldBe
                "There is no <absent> transformer"

        shouldThrow<TransformerNotFoundException> { actorService.getTransformerActor(("absent" to "sub-absent").toTransformerId()) }.message shouldBe
                "There is no <absent, sub-absent> transformer"
    }

    @Test
    fun getSerializerActor() {
        val serializerActorRef = mockk<ActorRef>()

        val actorService = GroupedByNameActorService(emptyList(),
                                                     serializerActorRef)

        actorService.getSerializerActor() shouldBe
                serializerActorRef
    }
}