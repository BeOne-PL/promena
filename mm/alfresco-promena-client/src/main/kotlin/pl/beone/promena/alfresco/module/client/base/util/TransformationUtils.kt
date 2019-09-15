package pl.beone.promena.alfresco.module.client.base.util

import pl.beone.promena.transformer.contract.transformation.Transformation

fun Transformation.createNodeName(): String =
    transformers.joinToString(", ") { it.transformerId.name }
