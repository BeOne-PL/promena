package pl.beone.promena.transformer.internal.model.metadata

data class MapMetadataBuilder internal constructor(private val metadata: MutableMap<String, Any>) {

    fun metadata(key: String, value: Any): MapMetadataBuilder =
            apply { metadata[key] = value }

    fun build(): MapMetadata = MapMetadata(metadata)

}