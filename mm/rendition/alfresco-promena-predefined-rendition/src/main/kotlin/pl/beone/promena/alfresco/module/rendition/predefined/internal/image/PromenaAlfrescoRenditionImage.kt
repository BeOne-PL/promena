package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import pl.beone.promena.alfresco.module.rendition.predefined.internal.supportedImageMagickConverterMediaTypes
import pl.beone.promena.alfresco.module.rendition.predefined.internal.supportedLibreOfficeConverterMediaTypes
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterParameters
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

internal fun determineTransformation(mediaType: MediaType, imageMagickConverterTransformation: Transformation.Single): Transformation? =
    when {
        supportedLibreOfficeConverterMediaTypes.any { it(mediaType) } ->
            libreOfficeConverterTransformation(APPLICATION_PDF, libreOfficeConverterParameters()) next
                    imageMagickConverterTransformation
        supportedImageMagickConverterMediaTypes.any { it(mediaType) } ->
            imageMagickConverterTransformation
        else ->
            null
    }