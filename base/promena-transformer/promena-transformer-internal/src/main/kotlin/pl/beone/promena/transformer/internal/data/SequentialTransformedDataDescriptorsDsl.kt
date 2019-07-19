package pl.beone.promena.transformer.internal.data

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun transformedDataDescriptor(transformedDataDescriptor: TransformedDataDescriptor): SequentialTransformedDataDescriptors =
        SequentialTransformedDataDescriptors.of(transformedDataDescriptor)

fun transformedDataDescriptor(data: Data, metadata: Metadata): SequentialTransformedDataDescriptors =
        SequentialTransformedDataDescriptors.of(TransformedDataDescriptor.of(data, metadata))

infix fun TransformedDataDescriptors.and(transformedDataDescriptor: TransformedDataDescriptor): SequentialTransformedDataDescriptors =
        SequentialTransformedDataDescriptors.of(getAll() + transformedDataDescriptor)

fun TransformedDataDescriptors.and(data: Data, metadata: Metadata): SequentialTransformedDataDescriptors =
        SequentialTransformedDataDescriptors.of(getAll() + TransformedDataDescriptor.of(data, metadata))