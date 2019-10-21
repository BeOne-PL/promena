package pl.beone.promena.transformer.contract.extension

import pl.beone.promena.transformer.contract.transformer.TransformerId

fun TransformerId.toPrettyString(): String =
    if (isSubNameSet()) {
        "($name, $subName)"
    } else {
        name
    }