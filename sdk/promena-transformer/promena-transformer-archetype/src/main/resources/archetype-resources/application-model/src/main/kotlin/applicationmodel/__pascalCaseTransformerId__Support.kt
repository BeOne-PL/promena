package ${package}.applicationmodel

import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Example
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Example2

object ${pascalCaseTransformerId}Support {

    @JvmStatic
    fun isSupported(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters) {
        dataDescriptor.descriptors.forEach { (_, mediaType) -> MediaTypeSupport.isSupported(mediaType, targetMediaType) }
        ParametersSupport.isSupported(parameters)
    }

    object MediaTypeSupport {
        private val supportedMediaType = setOf(
            TEXT_PLAIN to TEXT_PLAIN
        )

        @JvmStatic
        fun isSupported(mediaType: MediaType, targetMediaType: MediaType) {
            if (!supportedMediaType.contains(mediaType to targetMediaType)) {
                throw TransformationNotSupportedException.unsupportedMediaType(mediaType, targetMediaType)
            }
        }
    }

    object ParametersSupport {
        @JvmStatic
        fun isSupported(parameters: Parameters) {
            parameters.validate(Example.NAME, Example.CLASS, true)
            parameters.validate(Example2.NAME, Example2.CLASS, false)
        }

        private fun Parameters.validate(name: String, clazz: Class<*>, mandatory: Boolean) {
            try {
                get(name, clazz)
            } catch (e: NoSuchElementException) {
                if (mandatory) {
                    throw TransformationNotSupportedException.mandatoryParameter(name)
                }
            } catch (e: TypeConversionException) {
                throw TransformationNotSupportedException.unsupportedParameterType(name, clazz)
            }
        }
    }
}