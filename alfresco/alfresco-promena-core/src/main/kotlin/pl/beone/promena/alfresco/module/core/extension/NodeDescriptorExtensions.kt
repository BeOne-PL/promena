package pl.beone.promena.alfresco.module.core.extension

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.transformer.internal.extension.toPrettyString

fun NodeDescriptor.toPrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = NodeDescriptor.Single::toPrettyString) + "]"

private fun NodeDescriptor.Single.toPrettyString(): String =
    "<nodeRef=$nodeRef, metadata=${metadata.toPrettyString()}>"