package pl.beone.promena.communication.file.common.extension

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

fun CommunicationParameters.getDirectory(): File =
    File(get("directoryPath", String::class.java))