package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.contract.model.data.Data

fun Data.toBytesPrettyString(): String =
    try {
        "${getBytes().toMB().format(2)} MB"
    } catch (e: DataAccessibilityException) {
        "isn't accessible"
    }

fun Data.toLocationPrettyString(): String =
    try {
        getLocation().toString()
    } catch (e: UnsupportedOperationException) {
        "no location"
    }