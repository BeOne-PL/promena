package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun emptyDataDescriptor(): DataDescriptors.Empty = DataDescriptors.Empty

operator fun DataDescriptors.Empty.plus(single: DataDescriptors.Single): DataDescriptors.Single =
        single

fun dataDescriptor(data: Data, mediaType: MediaType, metadata: Metadata): DataDescriptors.Single =
        DataDescriptors.Single(data, mediaType, metadata)

operator fun DataDescriptors.Single.plus(descriptor: DataDescriptors.Single): DataDescriptors.Multi =
        DataDescriptors.Multi(descriptors + descriptor)

fun multiDataDescriptors(descriptor: DataDescriptors.Single,
                         descriptors: List<DataDescriptors.Single>): DataDescriptors.Multi =
        DataDescriptors.Multi(listOf(descriptor) + descriptors)

operator fun DataDescriptors.Multi.plus(descriptor: DataDescriptors.Single): DataDescriptors.Multi =
        DataDescriptors.Multi(descriptors + descriptor)

fun dataDescriptors(descriptors: List<DataDescriptors.Single>): DataDescriptors =
        when (descriptors.size) {
            0    -> DataDescriptors.Empty
            1    -> descriptors.first()
            else -> DataDescriptors.Multi.of(descriptors)
        }

