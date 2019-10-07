package pl.beone.promena.communication.memory.model.internal

import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

data class DefaultMemoryCommunicationParameters internal constructor(
    private val communicationParameters: CommunicationParameters
) : MemoryCommunicationParameters, CommunicationParameters by communicationParameters