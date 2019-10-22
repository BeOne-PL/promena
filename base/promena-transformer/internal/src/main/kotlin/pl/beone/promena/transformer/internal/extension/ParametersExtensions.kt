package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.model.Parameters

fun Parameters.toPrettyString(): String =
    getAll().toString()