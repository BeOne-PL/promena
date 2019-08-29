package pl.beone.transformation.module.service.external.alfresco.transform

import org.alfresco.repo.content.transform.AbstractContentTransformer2
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.TransformationOptions
import org.slf4j.LoggerFactory
import pl.beone.transformation.server.applicationmodel.mimetype.MimeType
import pl.beone.transformation.server.applicationmodel.mimetype.MimeType.*
import pl.beone.transformation.server.applicationmodel.mimetype.MimeTypeNotFound

class TransformationContentTransformer : AbstractContentTransformer2() {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformationContentTransformer::class.java)
    }

    override fun isTransformable(sourceMimetype: String, targetMimetype: String, options: TransformationOptions): Boolean {
        try {
            if (targetMimetype == APPLICATION_PDF.value) {
                return when (MimeType.convertToMimeType(sourceMimetype)) {
                    APPLICATION_OCTET_STREAM,
                    APPLICATION_ECMASCRIPT,
                    APPLICATION_JAVASCRIPT,
                    APPLICATION_JSON,
                    APPLICATION_MSWORD,
                    APPLICATION_RTF,
                    APPLICATION_TYPESCRIPT,
                    APPLICATION_VND_MOZILLA_XUL_XML,
                    APPLICATION_VND_MS_EXCEL,
                    APPLICATION_VND_MS_FONTOBJECT,
                    APPLICATION_VND_MS_POWERPOINT,
                    APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION,
                    APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET,
                    APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT,
                    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_PRESENTATIONML_PRESENTATION,
                    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET,
                    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT,
                    APPLICATION_VND_VISIO,
                    APPLICATION_XHTML_XML,
                    APPLICATION_XML,
                    APPLICATION_X_7Z_COMPRESSED,
                    APPLICATION_X_SH,
                    IMAGE_TIFF,
                    TEXT_CALENDAR,
                    TEXT_CSS,
                    TEXT_CSV,
                    TEXT_HTML,
                    TEXT_PLAIN,
                    TEXT_XML -> true

                    else -> false
                }
            }
        } catch (e: MimeTypeNotFound) {
            logger.debug("{}", e.message)
        }

        return false
    }

    override fun transformInternal(reader: ContentReader?, writer: ContentWriter?, options: TransformationOptions?) {
        throw RuntimeException("Impossible. Function is never invoked")
    }

}