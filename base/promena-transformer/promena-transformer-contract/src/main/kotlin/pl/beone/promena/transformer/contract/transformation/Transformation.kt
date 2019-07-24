package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.TransformerId

sealed class Transformation {

    data class Single private constructor(val transformerId: TransformerId,
                                          val targetMediaType: MediaType,
                                          val parameters: Parameters) : Transformation() {

        companion object {

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

    data class Composite private constructor(override val transformers: List<Single>) : Transformation() {

        companion object {

            @JvmStatic
            fun of(transformers: List<Single>): Composite =
                Composite(transformers)
        }
    }

    abstract val transformers: List<Single>

}