package pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf

import pl.beone.promena.alfresco.module.rendition.predefined.internal.supportedImageMagickConverterMediaTypes
import pl.beone.promena.alfresco.module.rendition.predefined.internal.supportedLibreOfficeConverterMediaTypes
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterParameters
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

internal fun determineTransformation(mediaType: MediaType): Transformation? =
    when {
        supportedLibreOfficeConverterMediaTypes.any { it(mediaType) } ->
            libreOfficeConverterTransformation(APPLICATION_PDF, libreOfficeConverterParameters())
        supportedImageMagickConverterMediaTypes.any { it(mediaType) } ->
            imageMagickConverterTransformation(APPLICATION_PDF, imageMagickConverterParameters())
        else ->
            null
    }