package pl.beone.promena.communication.file.configuration.internal

import org.springframework.core.env.Environment
import pl.beone.promena.core.common.utils.verifyIfItIsDirectoryAndYouCanCreateFile
import java.net.URI


internal fun Environment.getLocationAndVerify(): URI =
        URI(this.getProperty("communication.file.location")).apply {
            verifyIfItIsDirectoryAndYouCanCreateFile()
        }
