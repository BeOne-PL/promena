package pl.beone.promena.transformer.contract.transformer

/**
 * Represents an id of [Transformer][pl.beone.promena.transformer.contract.Transformer].
 * Each transformer consists of [name] and [subName]. [name] describes the kind of a transformation (`converter` for example) and
 * [subName] describes implementation details (`LibreOffice` for example).
 *
 * In case of constructing [Transformation][pl.beone.promena.transformer.contract.transformation.Transformation],
 * you can skip the [subName] not to bind the execution with specified transformer.
 *
 * @see TransformerIdDsl
 */
data class TransformerId internal constructor(
    val name: String,
    val subName: String?
) {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(name: String, subName: String? = null): TransformerId =
            TransformerId(name, subName)
    }

    fun isSubNameSet(): Boolean =
        subName != null
}