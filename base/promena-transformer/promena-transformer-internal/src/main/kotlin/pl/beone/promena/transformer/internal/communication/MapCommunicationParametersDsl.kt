package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters

fun communicationParameters(id: String, parameters: Map<String, Any> = emptyMap()): MapCommunicationParameters =
    MapCommunicationParameters.of(id, parameters)

operator fun CommunicationParameters.plus(entry: Pair<String, Any>): MapCommunicationParameters =
    MapCommunicationParameters.of(getId(), getAll() + entry)