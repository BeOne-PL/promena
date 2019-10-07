package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.ImageMagickConverterSupport
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.LibreOfficeConverterSupport
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

internal fun determineTransformation(mediaType: MediaType, targetMediaType: MediaType, transformation: Transformation.Single): Transformation =
    try {
        LibreOfficeConverterSupport.MediaTypeSupport.isSupported(mediaType, APPLICATION_PDF)
        ImageMagickConverterSupport.MediaTypeSupport.isSupported(APPLICATION_PDF, targetMediaType)

        libreOfficeConverterTransformation() next transformation
    } catch (e: TransformationNotSupportedException) {
        ImageMagickConverterSupport.MediaTypeSupport.isSupported(mediaType, targetMediaType)

        transformation
    }