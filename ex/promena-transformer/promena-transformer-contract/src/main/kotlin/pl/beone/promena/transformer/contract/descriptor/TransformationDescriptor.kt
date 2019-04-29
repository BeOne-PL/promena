package pl.beone.promena.transformer.contract.descriptor

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

data class TransformationDescriptor(val dataDescriptors: List<DataDescriptor>,
                                    val targetMediaType: MediaType,
                                    val parameters: Parameters)