@file:JvmName("MapMetadataDsl")

package pl.beone.promena.transformer.internal.model.metadata

import pl.beone.promena.transformer.contract.model.Metadata

fun emptyMetadata(): MapMetadata =
        metadata(emptyMap())

fun metadata(metadata: Map<String, Any>): MapMetadata =
        MapMetadata.of(metadata)

operator fun Metadata.plus(entry: Pair<String, Any>): MapMetadata =
        MapMetadata.of(getAll() + entry)