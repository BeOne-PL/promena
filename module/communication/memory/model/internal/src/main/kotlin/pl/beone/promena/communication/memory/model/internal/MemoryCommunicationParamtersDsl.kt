@file:JvmName("MemoryCommunicationParametersDsl")

package pl.beone.promena.communication.memory.model.internal

import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters

fun memoryCommunicationParameters(): CommunicationParameters =
    communicationParameters(MemoryCommunicationParametersConstants.ID)