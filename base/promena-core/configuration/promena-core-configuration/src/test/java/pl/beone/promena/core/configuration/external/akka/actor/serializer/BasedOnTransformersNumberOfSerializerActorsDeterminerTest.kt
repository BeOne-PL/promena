package pl.beone.promena.core.configuration.external.akka.actor.serializer

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class BasedOnTransformersNumberOfSerializerActorsDeterminerTest {

    companion object {
        private val basedOnTransformersNumberOfSerializerActorsDeterminer = BasedOnTransformersNumberOfSerializerActorsDeterminer()
    }

    @Test
    fun determine() {
        val transformerActorDescriptors = listOf(
            TransformerActorDescriptor("transformer".toTransformerId(), mockk(), 2),
            TransformerActorDescriptor(("transformer" to "sub").toTransformerId(), mockk(), 2),
            TransformerActorDescriptor("transformer2".toTransformerId(), mockk(), 3)
        )

        basedOnTransformersNumberOfSerializerActorsDeterminer.determine(transformerActorDescriptors) shouldBe 5
    }
}