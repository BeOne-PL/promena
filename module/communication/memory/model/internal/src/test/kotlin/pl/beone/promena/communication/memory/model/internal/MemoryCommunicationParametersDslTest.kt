package pl.beone.promena.communication.memory.model.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants

class MemoryCommunicationParametersDslTest {

    @Test
    fun memoryCommunicationParameters_() {
        memoryCommunicationParameters().getId() shouldBe MemoryCommunicationParametersConstants.ID
    }
}