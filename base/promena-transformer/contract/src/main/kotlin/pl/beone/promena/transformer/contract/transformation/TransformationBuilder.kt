package pl.beone.promena.transformer.contract.transformation

/**
 * Helps to construct a transformation.
 * It is aimed at Java developers. If you are Kotlin developer, it's a better idea to use DSL.
 */
class TransformationBuilder {

    private val transformers = ArrayList<Transformation.Single>()

    fun next(transformer: Transformation.Single): TransformationBuilder {
        transformers.add(transformer)

        return this
    }

    fun build(): Transformation =
        transformers.toTransformation()
}