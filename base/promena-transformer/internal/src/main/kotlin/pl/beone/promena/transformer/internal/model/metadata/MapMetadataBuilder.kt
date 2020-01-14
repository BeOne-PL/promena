package pl.beone.promena.transformer.internal.model.metadata

/**
 * Helps to construct [MapMetadata].
 * Targeted at Java developers. If you are Kotlin developer, it's a better idea to use DSL.
 *
 * @see MapMetadataDsl
 */
class MapMetadataBuilder {

    private val metadata = HashMap<String, Any>()

    fun add(key: String, value: Any): MapMetadataBuilder =
        apply { metadata[key] = value }

    fun build(): MapMetadata = metadata(metadata)
}