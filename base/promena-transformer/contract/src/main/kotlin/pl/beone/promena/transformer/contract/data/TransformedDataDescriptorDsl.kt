@file:JvmName("TransformedDataDescriptorDsl")

package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data

fun emptyTransformedDataDescriptor(): TransformedDataDescriptor.Empty =
    TransformedDataDescriptor.Empty

/**
 * ```
 * TransformedDataDescriptor.Empty +
 *      singleTransformedDataDescriptor(<Data>, <Metadata>)
 * ```
 *
 * @return [single]
 */
operator fun TransformedDataDescriptor.Empty.plus(single: TransformedDataDescriptor.Single): TransformedDataDescriptor.Single =
    single

fun singleTransformedDataDescriptor(data: Data, metadata: Metadata): TransformedDataDescriptor.Single =
    TransformedDataDescriptor.Single.of(data, metadata)

/**
 * ```
 * singleTransformedDataDescriptor(<Data>, <Metadata>) +
 *      singleTransformedDataDescriptor(<Data>, <Metadata>)
 * ```
 *
 * @return concatenation of `this` and [descriptor]
 */
operator fun TransformedDataDescriptor.Single.plus(descriptor: TransformedDataDescriptor.Single): TransformedDataDescriptor.Multi =
    TransformedDataDescriptor.Multi.of(descriptors + descriptor)

fun multiTransformedDataDescriptor(
    descriptor: TransformedDataDescriptor.Single,
    descriptors: List<TransformedDataDescriptor.Single>
): TransformedDataDescriptor.Multi =
    TransformedDataDescriptor.Multi.of(listOf(descriptor) + descriptors)

fun multiTransformedDataDescriptor(
    descriptor: TransformedDataDescriptor.Single,
    vararg descriptors: TransformedDataDescriptor.Single
): TransformedDataDescriptor.Multi =
    multiTransformedDataDescriptor(descriptor, descriptors.toList())

/**
 * ```
 * singleTransformedDataDescriptor(<Data>, <Metadata>) +
 *      singleTransformedDataDescriptor(<Data>, <Metadata>) +
 *      singleTransformedDataDescriptor(<Data>, <Metadata>)
 * ```
 *
 * @return concatenation of `this` and [descriptor]
 */
operator fun TransformedDataDescriptor.Multi.plus(descriptor: TransformedDataDescriptor.Single): TransformedDataDescriptor.Multi =
    TransformedDataDescriptor.Multi.of(descriptors + descriptor)

/**
 * ```
 * singleTransformedDataDescriptor(singleDataDescriptor(<Data>, <Metadata>)) +
 *      singleTransformedDataDescriptor(singleDataDescriptor(<Data>, <Metadata>))
 * ```
 *
 * @return concatenation of `this` and [descriptor]
 */
operator fun TransformedDataDescriptor.Multi.plus(descriptor: TransformedDataDescriptor.Multi): TransformedDataDescriptor.Multi =
    TransformedDataDescriptor.Multi.of(descriptors + descriptor.descriptors)

/**
 * @return [TransformedDataDescriptor.Empty] if [descriptors] is empty,
 *         [TransformedDataDescriptor.Single] if [descriptors] has one element
 *         and [TransformedDataDescriptor.Multi] if [descriptors] has many elements
 */
fun transformedDataDescriptor(descriptors: List<TransformedDataDescriptor.Single>): TransformedDataDescriptor =
    when (descriptors.size) {
        0 -> TransformedDataDescriptor.Empty
        1 -> descriptors.first()
        else -> TransformedDataDescriptor.Multi.of(descriptors)
    }

/**
 * @see transformedDataDescriptor
 */
fun transformedDataDescriptor(vararg descriptors: TransformedDataDescriptor.Single): TransformedDataDescriptor =
    transformedDataDescriptor(descriptors.toList())

/**
 * @see transformedDataDescriptor
 */
fun List<TransformedDataDescriptor.Single>.toTransformedDataDescriptor(): TransformedDataDescriptor =
    transformedDataDescriptor(this)