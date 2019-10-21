package pl.beone.promena.alfresco.module.core.extension

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor

fun NodeDescriptor.toPrettyString(): String =
    if (descriptors.size == 1) {
        descriptors[0].toPrettyString()
    } else {
        "[" + descriptors.joinToString(", ", transform = NodeDescriptor.Single::toPrettyString) + "]"
    }

private fun NodeDescriptor.Single.toPrettyString(): String =
    "(nodeRef=$nodeRef, metadata=${metadata.getAll()})"