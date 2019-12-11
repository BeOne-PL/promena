package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

fun TransformedDataDescriptor.toPrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = TransformedDataDescriptor.Single::toPrettyString) + "]"

fun TransformedDataDescriptor.Single.toPrettyString(): String =
    "<data=${data.toBytesPrettyString()}, metadata=${metadata.toPrettyString()}>"

fun TransformedDataDescriptor.toSimplePrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = TransformedDataDescriptor.Single::toSimplePrettyString) + "]"

fun TransformedDataDescriptor.Single.toSimplePrettyString(): String =
    "<metadata=${metadata.toPrettyString()}>"