package pl.beone.promena.transformer.contract.extension

import pl.beone.promena.transformer.applicationmodel.extension.toPrettyString
import pl.beone.promena.transformer.contract.transformation.Transformation

fun Transformation.toPrettyString(): String =
    if (transformers.size == 1) {
        transformers[0].toPrettyString()
    } else {
        "[" + transformers.joinToString(", ", transform = Transformation.Single::toPrettyString) + "]"
    }

private fun Transformation.Single.toPrettyString(): String =
    "(id=${transformerId.toPrettyString()}, targetMediaType=${targetMediaType.toPrettyString()}, parameters=${parameters.getAll()})"