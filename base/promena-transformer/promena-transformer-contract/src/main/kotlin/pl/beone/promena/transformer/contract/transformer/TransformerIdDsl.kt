@file:JvmName("TransformerIdDsl")

package pl.beone.promena.transformer.contract.transformer

fun transformerId(name: String, subName: String? = null): TransformerId =
    TransformerId.of(name, subName)

fun String.toTransformerId(): TransformerId =
    TransformerId.of(this)

fun Pair<String, String>.toTransformerId(): TransformerId =
    TransformerId.of(first, second)