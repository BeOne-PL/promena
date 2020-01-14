@file:JvmName("MapMetadataDsl")

package pl.beone.promena.transformer.internal.model.metadata

import pl.beone.promena.transformer.contract.model.Metadata

fun emptyMetadata(): MapMetadata =
    metadata(emptyMap())

fun metadata(metadata: Map<String, Any>): MapMetadata =
    MapMetadata.of(metadata)

/**
 * ```
 * emptyMetadata()
 *      + ("width" to 800)
 * ```
 *
 * @return concatenation of `this` and [entry]
 */
operator fun Metadata.plus(entry: Pair<String, Any>): MapMetadata =
    MapMetadata.of(getAll() + entry)

/**
 * ```
 * emptyMetadata() addIfNotNull
 *      ("width" to 800) addIfNotNull
 *      ("height" to null)
 * ```
 *
 * @return concatenation of `this` and [entry] if the value of [entry] isn't `null`
 */
infix fun Metadata.addIfNotNull(entry: Pair<String, Any?>): MapMetadata {
    val (key, value) = entry
    return if (value != null) {
        MapMetadata.of(getAll() + (key to value))
    } else {
        MapMetadata.of(this.getAll())
    }
}

/**
 * ```
 * metadata(mapOf("width" to 800)) +
 *      metadata(mapOf("height" to 600))
 * ```
 *
 * @return concatenation of `this` and [parameters]
 */
operator fun Metadata.plus(parameters: Metadata): MapMetadata =
    MapMetadata.of(getAll() + parameters.getAll())