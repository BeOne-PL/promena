package pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_MSWORD
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_EXCEL
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_EXCEL_SHEET_MACRO_ENABLED_12
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_EXCEL_TEMPLATE_MACRO_ENABLED_12
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_POWERPOINT
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_POWERPOINT_PRESENTATION_MACROENABLED_12
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_POWERPOINT_SLIDESHOW_MACROENABLED_12
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_POWERPOINT_TEMPLATE_MACROENABLED_12
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_MS_WORD_DOCUMENT_MACROENABLED_12
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION_TEMPLATE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET_TEMPLATE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_PRESENTATION
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_SLIDESHOW
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_TEMPLATE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_TEMPLATE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_TEMPLATE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_GIF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_JPEG
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_PNG
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_TIFF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_CSV
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_HTML
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterParameters
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

private val supportedLibreOfficeConverterMediaTypes = listOf<(MediaType) -> Boolean>(
    { it == APPLICATION_MSWORD },
    { it == APPLICATION_VND_MS_EXCEL },
    { it == APPLICATION_VND_MS_EXCEL_SHEET_MACRO_ENABLED_12 },
    { it == APPLICATION_VND_MS_EXCEL_TEMPLATE_MACRO_ENABLED_12 },
    { it == APPLICATION_VND_MS_POWERPOINT },
    { it == APPLICATION_VND_MS_POWERPOINT_PRESENTATION_MACROENABLED_12 },
    { it == APPLICATION_VND_MS_POWERPOINT_SLIDESHOW_MACROENABLED_12 },
    { it == APPLICATION_VND_MS_POWERPOINT_TEMPLATE_MACROENABLED_12 },
    { it == APPLICATION_VND_MS_WORD_DOCUMENT_MACROENABLED_12 },
    { it == APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION },
    { it == APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION_TEMPLATE },
    { it == APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET },
    { it == APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET_TEMPLATE },
    { it == APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_PRESENTATION },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_SLIDESHOW },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_TEMPLATE },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_TEMPLATE },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT },
    { it == APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_TEMPLATE },
    { it.mimeType == TEXT_CSV.mimeType },
    { it.mimeType == TEXT_HTML.mimeType },
    { it.mimeType == TEXT_PLAIN.mimeType },
    { it.mimeType == TEXT_XML.mimeType }
)

private val supportedImageMagickConverterMediaTypes = listOf<(MediaType) -> Boolean>(
    { it == IMAGE_PNG },
    { it == IMAGE_JPEG },
    { it == IMAGE_GIF },
    { it == IMAGE_TIFF }
)

internal fun determineTransformation(mediaType: MediaType): Transformation? =
    when {
        supportedLibreOfficeConverterMediaTypes.any { it(mediaType) } ->
            libreOfficeConverterTransformation(APPLICATION_PDF, libreOfficeConverterParameters())
        supportedImageMagickConverterMediaTypes.any { it(mediaType) } ->
            imageMagickConverterTransformation(APPLICATION_PDF, imageMagickConverterParameters())
        else ->
            null
    }