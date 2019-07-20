@file:JvmName("TransformedDataDescriptorsDsl")

package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun emptyTransformedDataDescriptor(): TransformedDataDescriptors.Empty = TransformedDataDescriptors.Empty

operator fun TransformedDataDescriptors.Empty.plus(single: TransformedDataDescriptors.Single): TransformedDataDescriptors.Single =
        single

fun transformedDataDescriptor(data: Data, metadata: Metadata): TransformedDataDescriptors.Single =
        TransformedDataDescriptors.Single(data, metadata)

operator fun TransformedDataDescriptors.Single.plus(descriptor: TransformedDataDescriptors.Single): TransformedDataDescriptors.Multi =
        TransformedDataDescriptors.Multi(descriptors + descriptor)

fun multiTransformedDataDescriptors(descriptor: TransformedDataDescriptors.Single,
                                    descriptors: List<TransformedDataDescriptors.Single>): TransformedDataDescriptors.Multi =
        TransformedDataDescriptors.Multi(listOf(descriptor) + descriptors)

operator fun TransformedDataDescriptors.Multi.plus(descriptor: TransformedDataDescriptors.Single): TransformedDataDescriptors.Multi =
        TransformedDataDescriptors.Multi(descriptors + descriptor)

fun transformedDataDescriptors(descriptors: List<TransformedDataDescriptors.Single>): TransformedDataDescriptors =
        when (descriptors.size) {
            0    -> TransformedDataDescriptors.Empty
            1    -> descriptors.first()
            else -> TransformedDataDescriptors.Multi(descriptors)
        }

fun transformedDataDescriptors(vararg descriptors: TransformedDataDescriptors.Single): TransformedDataDescriptors =
        transformedDataDescriptors(descriptors.toList())

fun List<TransformedDataDescriptors.Single>.toTransformedDataDescriptors(): TransformedDataDescriptors =
        transformedDataDescriptors(this)