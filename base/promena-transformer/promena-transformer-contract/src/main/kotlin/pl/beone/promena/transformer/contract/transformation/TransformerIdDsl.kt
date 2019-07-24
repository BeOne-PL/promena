package pl.beone.promena.transformer.contract.transformation

fun transformerId(name: String, implementationName: String? = null): TransformerId =
    TransformerId.of(name, implementationName)