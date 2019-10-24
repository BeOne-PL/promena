package pl.beone.promena.alfresco.module.core.contract.transformation.definition

import org.alfresco.service.namespace.QName
import java.io.Serializable

typealias Converter = (value: Any) -> Serializable

interface PromenaTransformationMetadataMappingDefinition {

    fun getKey(): String

    fun getProperty(): QName

    fun getConverter(): Converter = { it as Serializable }
}