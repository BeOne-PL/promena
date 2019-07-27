package pl.beone.promena.transformer.contract.transformation

class TransformationBuilder {

    private val transformers = ArrayList<Transformation.Single>()

    fun next(transformer: Transformation.Single): TransformationBuilder {
        transformers.add(transformer)

        return this
    }

    fun build(): Transformation =
        transformers.toTransformation()
}