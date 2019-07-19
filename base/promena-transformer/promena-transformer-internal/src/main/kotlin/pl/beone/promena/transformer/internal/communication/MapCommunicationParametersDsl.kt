package pl.beone.promena.transformer.internal.communication

fun communicationParameters(id: String, parameters: Map<String, Any>? = null): MapCommunicationParameters =
        MapCommunicationParameters.of(id, parameters)

infix fun MapCommunicationParameters.add(entry: Pair<String, Any>): MapCommunicationParameters =
        MapCommunicationParameters.of(getId(), getAll() + entry)