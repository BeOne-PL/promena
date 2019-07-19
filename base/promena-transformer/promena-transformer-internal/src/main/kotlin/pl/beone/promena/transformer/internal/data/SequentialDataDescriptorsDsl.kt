package pl.beone.promena.transformer.internal.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

fun dataDescriptor(dataDescriptor: DataDescriptor): SequentialDataDescriptors =
        SequentialDataDescriptors.of(dataDescriptor)

fun dataDescriptor(data: Data, mediaType: MediaType, metadata: Metadata): SequentialDataDescriptors =
        SequentialDataDescriptors.of(DataDescriptor.of(data, mediaType, metadata))

infix fun DataDescriptors.and(dataDescriptor: DataDescriptor): SequentialDataDescriptors =
        SequentialDataDescriptors.of(getAll() + dataDescriptor)

fun DataDescriptors.and(data: Data, mediaType: MediaType, metadata: Metadata): SequentialDataDescriptors =
        SequentialDataDescriptors.of(getAll() + DataDescriptor.of(data, mediaType, metadata))