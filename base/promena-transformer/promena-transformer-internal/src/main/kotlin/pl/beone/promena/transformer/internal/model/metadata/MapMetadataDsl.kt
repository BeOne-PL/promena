package pl.beone.promena.transformer.internal.model.metadata

fun emptyMetadata(): MapMetadata =
        metadata()

fun metadata(metadata: Map<String, Any> = emptyMap()): MapMetadata =
        MapMetadata.of(metadata)

infix fun MapMetadata.add(entry: Pair<String, Any>): MapMetadata =
        MapMetadata.of(getAll() + entry)