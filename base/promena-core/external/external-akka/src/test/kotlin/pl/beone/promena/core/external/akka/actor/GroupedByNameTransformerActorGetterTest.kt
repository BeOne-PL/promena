package pl.beone.promena.core.external.akka.actor

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class GroupedByNameTransformerActorGetterTest {

    @Test
    fun getTransformerActor() {
        val veryEmptyTransformerId = ("empty" to "very").toTransformerId()
        val veryEmptyTransformerActorDescriptor = TransformerActorDescriptor(veryEmptyTransformerId, mockk(), 1)

        val actorGetter = GroupedByNameTransformerActorGetter(listOf(veryEmptyTransformerActorDescriptor))

        actorGetter.get("empty".toTransformerId()) shouldBeSameInstanceAs
                veryEmptyTransformerActorDescriptor.actorRef

        actorGetter.get(veryEmptyTransformerId) shouldBeSameInstanceAs
                veryEmptyTransformerActorDescriptor.actorRef

        shouldThrow<TransformerNotFoundException> {
            actorGetter.get("absent".toTransformerId())
        }.message shouldBe "There is no <absent> transformer"

        shouldThrow<TransformerNotFoundException> {
            actorGetter.get(("empty" to "not-much").toTransformerId())
        }.message shouldBe "There is no <(empty, not-much)> transformer"

        shouldThrow<TransformerNotFoundException> {
            actorGetter.get("absent".toTransformerId())
        }.message shouldBe "There is no <absent> transformer"

        shouldThrow<TransformerNotFoundException> {
            actorGetter.get(("absent" to "sub-absent").toTransformerId())
        }.message shouldBe "There is no <(absent, sub-absent)> transformer"
    }
}