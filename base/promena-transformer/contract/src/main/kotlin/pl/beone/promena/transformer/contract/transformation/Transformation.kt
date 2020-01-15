package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation.Composite
import pl.beone.promena.transformer.contract.transformer.TransformerId

/**
 * Represents a flow of transformations executing by transformers. The order matters.
 * A transformation can be made up of many [Transformation.Single]. At least one [Transformation.Single] is required.
 * Many [Transformation.Single] make up [Composite] transformation.
 *
 * @see TransformationDsl
 * @see TransformationBuilder
 */
sealed class Transformation {

    /**
     * @param transformerId the transformer id on which the transformation should be performed.
     *                      If you want to perform given transformation on the specific transformer,
     *                      you should also set [TransformerId.subName][pl.beone.promena.transformer.contract.transformer.TransformerId.subName]
     * @param targetMediaType the target type of the transformation
     * @param parameters the parameters of the transformation
     */
    data class Single internal constructor(
        val transformerId: TransformerId,
        val targetMediaType: MediaType,
        val parameters: Parameters
    ) : Transformation() {

        companion object {
            @JvmStatic
            fun of(transformerId: TransformerId, targetMediaType: MediaType, parameters: Parameters): Single =
                Single(transformerId, targetMediaType, parameters)

            @JvmStatic
            fun of(transformerName: String, transformerSubName: String, targetMediaType: MediaType, parameters: Parameters): Single =
                Single(TransformerId.of(transformerName, transformerSubName), targetMediaType, parameters)

            @JvmStatic
            fun of(transformerName: String, targetMediaType: MediaType, parameters: Parameters): Single =
                Single(TransformerId.of(transformerName, null), targetMediaType, parameters)
        }

        override val transformers: List<Single>
            get() = listOf(this)
    }

    data class Composite internal constructor(
        override val transformers: List<Single>
    ) : Transformation() {

        companion object {
            @JvmStatic
            fun of(transformers: List<Single>): Composite =
                Composite(transformers)
        }
    }

    /**
     * The list of [Transformation.Single] making up the whole transformation.
     */
    abstract val transformers: List<Single>
}