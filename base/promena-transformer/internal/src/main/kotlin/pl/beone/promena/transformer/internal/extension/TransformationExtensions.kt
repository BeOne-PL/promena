package pl.beone.promena.transformer.internal.extension

import pl.beone.promena.transformer.contract.transformation.Transformation

fun Transformation.toPrettyString(): String =
    "[" + transformers.joinToString(", ", transform = Transformation.Single::toPrettyString) + "]"

private fun Transformation.Single.toPrettyString(): String =
    "<transformerId=${transformerId.toPrettyString()}, targetMediaType=${targetMediaType.toPrettyString()}, parameters=${parameters.getAll()}>"