package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.data.DataDescriptor

interface InternalCommunicationConverter {

    fun convert(dataDescriptor: DataDescriptor): DataDescriptor
}