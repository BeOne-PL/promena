@file:JvmName("DataDescriptorsDsl")

package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun emptyDataDescriptor(): DataDescriptor.Empty = DataDescriptor.Empty

operator fun DataDescriptor.Empty.plus(single: DataDescriptor.Single): DataDescriptor.Single =
        single

fun dataDescriptor(data: Data, mediaType: MediaType, metadata: Metadata): DataDescriptor.Single =
        DataDescriptor.Single(data, mediaType, metadata)

operator fun DataDescriptor.Single.plus(descriptor: DataDescriptor.Single): DataDescriptor.Multi =
        DataDescriptor.Multi(descriptors + descriptor)

fun multiDataDescriptor(descriptor: DataDescriptor.Single,
                        descriptors: List<DataDescriptor.Single>): DataDescriptor.Multi =
        DataDescriptor.Multi(listOf(descriptor) + descriptors)

operator fun DataDescriptor.Multi.plus(descriptor: DataDescriptor.Single): DataDescriptor.Multi =
        DataDescriptor.Multi(descriptors + descriptor)

fun dataDescriptors(descriptors: List<DataDescriptor.Single>): DataDescriptor =
        when (descriptors.size) {
            0    -> DataDescriptor.Empty
            1    -> descriptors.first()
            else -> DataDescriptor.Multi(descriptors.toList())
        }

fun dataDescriptors(vararg descriptors: DataDescriptor.Single): DataDescriptor =
        dataDescriptors(descriptors.toList())

fun List<DataDescriptor.Single>.toDataDescriptor(): DataDescriptor =
        dataDescriptors(this)