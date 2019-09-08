package pl.beone.promena.alfresco.module.client.base.applicationmodel.communication

import java.io.File

data class ExternalCommunication(
    val id: String,
    val directory: File? = null
)