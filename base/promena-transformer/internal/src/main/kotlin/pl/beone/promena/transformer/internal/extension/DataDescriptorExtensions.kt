package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.data.DataDescriptor

fun DataDescriptor.toPrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = DataDescriptor.Single::toPrettyString) + "]"

fun DataDescriptor.Single.toPrettyString(): String =
    "<data=(${data.toLocationPrettyString()}, ${data.toBytesPrettyString()}), mediaType=${mediaType.toPrettyString()}, metadata=${metadata.toPrettyString()}>"

fun DataDescriptor.toSimplePrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = DataDescriptor.Single::toSimplePrettyString) + "]"

fun DataDescriptor.Single.toSimplePrettyString(): String =
    "<data=${data.toLocationPrettyString()}, mediaType=${mediaType.toPrettyString()}, metadata=${metadata.toPrettyString()}>"
