@file:JvmName("DataDescriptorDsl")

package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun emptyDataDescriptor(): DataDescriptor.Empty = DataDescriptor.Empty

operator fun DataDescriptor.Empty.plus(single: DataDescriptor.Single): DataDescriptor.Single =
    single

fun singleDataDescriptor(data: Data, mediaType: MediaType, metadata: Metadata): DataDescriptor.Single =
    DataDescriptor.Single.of(data, mediaType, metadata)

operator fun DataDescriptor.Single.plus(descriptor: DataDescriptor.Single): DataDescriptor.Multi =
    DataDescriptor.Multi.of(descriptors + descriptor)

fun multiDataDescriptor(descriptor: DataDescriptor.Single, descriptors: List<DataDescriptor.Single>): DataDescriptor.Multi =
    DataDescriptor.Multi.of(listOf(descriptor) + descriptors)

fun multiDataDescriptor(descriptor: DataDescriptor.Single, vararg descriptors: DataDescriptor.Single): DataDescriptor.Multi =
    multiDataDescriptor(descriptor, descriptors.toList())

operator fun DataDescriptor.Multi.plus(descriptor: DataDescriptor.Single): DataDescriptor.Multi =
    DataDescriptor.Multi.of(descriptors + descriptor)

fun dataDescriptor(descriptors: List<DataDescriptor.Single>): DataDescriptor =
    when (descriptors.size) {
        0    -> DataDescriptor.Empty
        1    -> descriptors.first()
        else -> DataDescriptor.Multi.of(descriptors.toList())
    }

fun dataDescriptor(vararg descriptors: DataDescriptor.Single): DataDescriptor =
    dataDescriptor(descriptors.toList())

fun List<DataDescriptor.Single>.toDataDescriptor(): DataDescriptor =
    dataDescriptor(this)