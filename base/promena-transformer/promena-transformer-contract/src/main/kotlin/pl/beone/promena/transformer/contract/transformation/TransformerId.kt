package pl.beone.promena.transformer.contract.transformation

data class TransformerId internal constructor(val name: String,
                                              val implementationName: String?) {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun of(name: String, implementationName: String? = null): TransformerId =
            TransformerId(name, implementationName)
    }
}