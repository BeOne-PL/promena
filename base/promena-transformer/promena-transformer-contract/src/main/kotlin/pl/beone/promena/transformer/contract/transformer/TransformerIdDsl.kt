package pl.beone.promena.transformer.contract.transformer

fun transformerId(name: String, detailName: String? = null): TransformerId =
    TransformerId.of(name, detailName)