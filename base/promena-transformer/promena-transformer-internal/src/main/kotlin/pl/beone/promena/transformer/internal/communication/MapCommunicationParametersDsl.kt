package pl.beone.promena.transformer.internal.communication

fun communicationParameters(): MapCommunicationParameters =
        MapCommunicationParameters.empty()

infix fun MapCommunicationParameters.add(entry: Pair<String, Any>): MapCommunicationParameters =
        MapCommunicationParameters.of(getId(), getAll() + entry)