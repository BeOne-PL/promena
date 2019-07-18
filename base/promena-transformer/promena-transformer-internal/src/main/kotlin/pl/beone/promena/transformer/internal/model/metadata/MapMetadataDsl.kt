package pl.beone.promena.transformer.internal.model.metadata

fun metadata(): MapMetadata =
        MapMetadata.empty()

infix fun MapMetadata.add(entry: Pair<String, Any>): MapMetadata =
        MapMetadata.of(getAll() + entry)