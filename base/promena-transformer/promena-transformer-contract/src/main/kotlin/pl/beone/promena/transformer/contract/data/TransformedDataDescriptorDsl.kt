@file:JvmName("TransformedDataDescriptorDsl")

package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun emptyTransformedDataDescriptor(): TransformedDataDescriptor.Empty = TransformedDataDescriptor.Empty

operator fun TransformedDataDescriptor.Empty.plus(single: TransformedDataDescriptor.Single): TransformedDataDescriptor.Single =
        single

fun singleTransformedDataDescriptor(data: Data, metadata: Metadata): TransformedDataDescriptor.Single =
        TransformedDataDescriptor.Single(data, metadata)

operator fun TransformedDataDescriptor.Single.plus(descriptor: TransformedDataDescriptor.Single): TransformedDataDescriptor.Multi =
        TransformedDataDescriptor.Multi(descriptors + descriptor)

fun multiTransformedDataDescriptor(descriptor: TransformedDataDescriptor.Single,
                                   descriptors: List<TransformedDataDescriptor.Single>): TransformedDataDescriptor.Multi =
        TransformedDataDescriptor.Multi(listOf(descriptor) + descriptors)

operator fun TransformedDataDescriptor.Multi.plus(descriptor: TransformedDataDescriptor.Single): TransformedDataDescriptor.Multi =
        TransformedDataDescriptor.Multi(descriptors + descriptor)

fun transformedDataDescriptor(descriptors: List<TransformedDataDescriptor.Single>): TransformedDataDescriptor =
        when (descriptors.size) {
            0    -> TransformedDataDescriptor.Empty
            1    -> descriptors.first()
            else -> TransformedDataDescriptor.Multi(descriptors)
        }

fun transformedDataDescriptor(vararg descriptors: TransformedDataDescriptor.Single): TransformedDataDescriptor =
        transformedDataDescriptor(descriptors.toList())

fun List<TransformedDataDescriptor.Single>.toTransformedDataDescriptor(): TransformedDataDescriptor =
        transformedDataDescriptor(this)