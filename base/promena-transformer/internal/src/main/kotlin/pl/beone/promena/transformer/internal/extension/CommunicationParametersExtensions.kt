package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.communication.CommunicationParameters

fun CommunicationParameters.toPrettyString(): String =
    getAll().toString()