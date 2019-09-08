package pl.beone.promena.communication.file.common.extension

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

fun CommunicationParameters.getExternalCommunicationDirectory(): File =
    File(get("directoryPath", String::class.java))

fun CommunicationParameters.getInternalCommunicationDirectory(): File =
    get("directory", File::class.java)