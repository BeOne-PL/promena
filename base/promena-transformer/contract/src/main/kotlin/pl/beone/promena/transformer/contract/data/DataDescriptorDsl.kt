@file:JvmName("DataDescriptorDsl")

package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data

fun emptyDataDescriptor(): DataDescriptor.Empty = DataDescriptor.Empty

/**
 * ```
 * DataDescriptor.Empty +
 *      singleDataDescriptor(<Data>, APPLICATION_PDF, <Metadata>)
 * ```
 *
 * @return [single]
 */
operator fun DataDescriptor.Empty.plus(single: DataDescriptor.Single): DataDescriptor.Single =
    single

fun singleDataDescriptor(data: Data, mediaType: MediaType, metadata: Metadata): DataDescriptor.Single =
    DataDescriptor.Single.of(data, mediaType, metadata)

/**
 * ```
 * singleDataDescriptor(<Data>, TEXT_PLAIN, <Metadata>) +
 *      singleDataDescriptor(<Data>, APPLICATION_PDF, <Metadata>)
 * ```
 *
 * @return concatenation of `this` and [descriptor]
 */
operator fun DataDescriptor.Single.plus(descriptor: DataDescriptor.Single): DataDescriptor.Multi =
    DataDescriptor.Multi.of(descriptors + descriptor)

fun multiDataDescriptor(descriptor: DataDescriptor.Single, descriptors: List<DataDescriptor.Single>): DataDescriptor.Multi =
    DataDescriptor.Multi.of(listOf(descriptor) + descriptors)

fun multiDataDescriptor(descriptor: DataDescriptor.Single, vararg descriptors: DataDescriptor.Single): DataDescriptor.Multi =
    multiDataDescriptor(descriptor, descriptors.toList())

/**
 * ```
 * singleDataDescriptor(<Data>, TEXT_PLAIN, <Metadata>) +
 *      singleDataDescriptor(<Data>, TEXT_XML, <Metadata>) +
 *      singleDataDescriptor(<Data>, APPLICATION_PDF, <Metadata>)
 * ```
 *
 * @return concatenation of `this` and [descriptor]
 */
operator fun DataDescriptor.Multi.plus(descriptor: DataDescriptor.Single): DataDescriptor.Multi =
    DataDescriptor.Multi.of(descriptors + descriptor)

/**
 * ```
 * multiDataDescriptor(singleDataDescriptor(<Data>, TEXT_PLAIN, <Metadata>)) +
 *      multiDataDescriptor(singleDataDescriptor(<Data>, APPLICATION_PDF, <Metadata>))
 * ```
 *
 * @return concatenation of `this` and [descriptor]
 */
operator fun DataDescriptor.Multi.plus(descriptor: DataDescriptor.Multi): DataDescriptor.Multi =
    DataDescriptor.Multi.of(descriptors + descriptor.descriptors)

/**
 * @return [DataDescriptor.Empty] if [descriptors] is empty,
 *         [DataDescriptor.Single] if [descriptors] has one element
 *         and [DataDescriptor.Multi] if [descriptors] has many elements
 */
fun dataDescriptor(descriptors: List<DataDescriptor.Single>): DataDescriptor =
    when (descriptors.size) {
        0 -> DataDescriptor.Empty
        1 -> descriptors.first()
        else -> DataDescriptor.Multi.of(descriptors.toList())
    }

/**
 * @see dataDescriptor
 */
fun dataDescriptor(vararg descriptors: DataDescriptor.Single): DataDescriptor =
    dataDescriptor(descriptors.toList())

/**
 * @see dataDescriptor
 */
fun List<DataDescriptor.Single>.toDataDescriptor(): DataDescriptor =
    dataDescriptor(this)