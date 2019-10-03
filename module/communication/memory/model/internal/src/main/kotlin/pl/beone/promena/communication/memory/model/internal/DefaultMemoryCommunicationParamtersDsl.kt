@file:JvmName("DefaultMemoryCommunicationParametersDsl")

package pl.beone.promena.communication.memory.model.internal

import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters

fun memoryCommunicationParameters(): DefaultMemoryCommunicationParameters =
    DefaultMemoryCommunicationParameters(
        communicationParameters(MemoryCommunicationParameters.ID)
    )