package pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.ImageMagickConverterSupport
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.LibreOfficeConverterSupport
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

internal fun determineTransformation(mediaType: MediaType, targetMediaType: MediaType): Transformation =
    try {
        LibreOfficeConverterSupport.MediaTypeSupport.isSupported(mediaType, targetMediaType)

        libreOfficeConverterTransformation()
    } catch (e: TransformationNotSupportedException) {
        ImageMagickConverterSupport.MediaTypeSupport.isSupported(mediaType, targetMediaType)

        imageMagickConverterTransformation(targetMediaType)
    }