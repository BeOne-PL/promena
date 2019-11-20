package ${package}.applicationmodel

import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Mandatory
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.Optional
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.OptionalLimitedValue

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
            parameters.validate(Mandatory.NAME, Mandatory.CLASS, true)
            parameters.validate(Optional.NAME, Optional.CLASS, false)
            parameters.validate(OptionalLimitedValue.NAME, OptionalLimitedValue.CLASS, false, "<0, 10>") { (0..10).contains(it) }
        }

        private fun <T> Parameters.validate(
            name: String,
            clazz: Class<T>,
            mandatory: Boolean,
            valueVerifierMessage: String? = null,
            valueVerifier: (T) -> Boolean = { true }
        ) {
            try {
                val value = get(name, clazz)
                if (!valueVerifier(value)) {
                    throw TransformationNotSupportedException.unsupportedParameterValue(name, value, valueVerifierMessage)
                }
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