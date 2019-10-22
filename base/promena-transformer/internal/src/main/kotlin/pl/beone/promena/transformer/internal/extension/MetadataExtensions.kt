package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.model.Metadata

fun Metadata.toPrettyString(): String =
    getAll().toString()