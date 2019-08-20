package pl.beone.promena.communication.file.utils

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.net.URI

fun CommunicationParameters.getLocation(): URI =
    get("location", URI::class.java)