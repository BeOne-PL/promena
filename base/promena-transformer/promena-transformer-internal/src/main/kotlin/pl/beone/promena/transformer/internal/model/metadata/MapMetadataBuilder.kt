package pl.beone.promena.transformer.internal.model.metadata

class MapMetadataBuilder {

    private val metadata = HashMap<String, Any>()

    fun add(key: String, value: Any): MapMetadataBuilder =
            apply { metadata[key] = value }

    fun build(): MapMetadata = metadata(metadata)

}