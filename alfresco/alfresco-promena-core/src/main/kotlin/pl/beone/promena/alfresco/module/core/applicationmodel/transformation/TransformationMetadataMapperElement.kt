package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import org.alfresco.service.namespace.QName
import java.io.Serializable

typealias Converter = (value: Any) -> Serializable

data class TransformationMetadataMapperElement internal constructor(
    val key: String,
    val property: QName,
    val converter: Converter
) {

    companion object {
        @JvmStatic
        fun of(key: String, property: QName, converter: Converter = { it as Serializable }): TransformationMetadataMapperElement =
            TransformationMetadataMapperElement(key, property, converter)
    }
}