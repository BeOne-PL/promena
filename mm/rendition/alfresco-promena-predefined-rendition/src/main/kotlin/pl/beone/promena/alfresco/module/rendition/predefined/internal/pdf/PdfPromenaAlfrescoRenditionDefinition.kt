package pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf

import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterParameters
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

class PdfPromenaAlfrescoRenditionDefinition : AlfrescoPromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "pdf"

    override fun getTransformation(): Transformation =
        libreOfficeConverterTransformation(
            APPLICATION_PDF,
            libreOfficeConverterParameters()
        )
}