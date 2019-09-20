package pl.beone.promena.alfresco.module.rendition.extension

import pl.beone.promena.alfresco.module.client.base.util.createNodeName
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

fun AlfrescoPromenaRenditionDefinition.getTransformationNodeName(): String =
    getTransformation().createNodeName()