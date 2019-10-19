@file:JvmName("TransformationMetadataMapperElementDsl")

package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import org.alfresco.service.namespace.QName
import java.io.Serializable

fun transformationMetadataMapperElement(key: String, property: QName, converter: Converter = { it as Serializable }): TransformationMetadataMapperElement =
    TransformationMetadataMapperElement.of(key, property, converter)