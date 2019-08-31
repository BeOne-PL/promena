package pl.beone.promena.transformer.contract.transformer

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