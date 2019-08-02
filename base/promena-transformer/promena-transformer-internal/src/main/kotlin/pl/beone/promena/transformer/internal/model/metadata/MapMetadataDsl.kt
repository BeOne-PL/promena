@file:JvmName("MapMetadataDsl")

package pl.beone.promena.transformer.internal.model.metadata

import pl.beone.promena.transformer.contract.model.Metadata

fun emptyMetadata(): MapMetadata =
    metadata(emptyMap())

fun metadata(metadata: Map<String, Any>): MapMetadata =
    MapMetadata.of(metadata)

operator fun Metadata.plus(entry: Pair<String, Any>): MapMetadata =
    MapMetadata.of(getAll() + entry)

infix fun Metadata.addIfNotNull(entry: Pair<String, Any?>): MapMetadata {
    val (key, value) = entry
    return if (value != null) {
        MapMetadata.of(getAll() + (key to value))
    } else {
        MapMetadata.of(this.getAll())
    }
}

operator fun Metadata.plus(parameters: Metadata): MapMetadata =
    MapMetadata.of(getAll() + parameters.getAll())