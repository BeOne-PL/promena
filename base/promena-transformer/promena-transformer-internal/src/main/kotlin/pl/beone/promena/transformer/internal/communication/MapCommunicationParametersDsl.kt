@file:JvmName("MapCommunicationParametersDsl")

package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters

fun communicationParameters(id: String): MapCommunicationParameters =
        MapCommunicationParameters.of(id)

fun communicationParameters(id: String, parameters: Map<String, Any>): MapCommunicationParameters =
        MapCommunicationParameters.of(id, parameters)

operator fun CommunicationParameters.plus(entry: Pair<String, Any>): MapCommunicationParameters =
        MapCommunicationParameters.of(getId(), getAll() + entry)