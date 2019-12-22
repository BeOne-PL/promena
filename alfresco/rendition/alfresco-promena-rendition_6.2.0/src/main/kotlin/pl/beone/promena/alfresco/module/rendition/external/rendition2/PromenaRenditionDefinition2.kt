package pl.beone.promena.alfresco.module.rendition.external.rendition2

import org.alfresco.repo.rendition2.RenditionDefinition2

internal class PromenaRenditionDefinition2(
    private val renditionName: String
) : RenditionDefinition2 {

    override fun getRenditionName(): String =
        renditionName

    override fun getTargetMimetype(): String? =
        null

    override fun getTransformOptions(): Map<String, String>? =
        null
}