package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data

fun DataDescriptor.toPrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = DataDescriptor.Single::toPrettyString) + "]"

fun DataDescriptor.Single.toPrettyString(): String =
    "<data=(${data.toLocationPrettyString()}, ${data.getBytes().toMB().format(2)} MB), mediaType=${mediaType.toPrettyString()}, metadata=${metadata.toPrettyString()}>"

fun DataDescriptor.toSimplePrettyString(): String =
    "[" + descriptors.joinToString(", ", transform = DataDescriptor.Single::toSimplePrettyString) + "]"

fun DataDescriptor.Single.toSimplePrettyString(): String =
    "<data=${data.toLocationPrettyString()}, mediaType=${mediaType.toPrettyString()}, metadata=${metadata.toPrettyString()}>"

fun Data.toLocationPrettyString(): String =
    try {
        getLocation().toString()
    } catch (e: UnsupportedOperationException) {
        "no location"
    }
