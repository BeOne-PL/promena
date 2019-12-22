@file:Suppress("DEPRECATION")

package pl.beone.promena.alfresco.module.rendition.mock.transformer

import org.alfresco.repo.content.transform.ContentTransformer
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.TransformationOptions
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.BOOLEAN
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.LONG
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.STRING

class MockContentTransformer : ContentTransformer {

    override fun isTransformableSize(sourceMimetype: String?, sourceSize: Long, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun isTransformableMimetype(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun getName(): String =
        STRING

    override fun getComments(available: Boolean): String =
        STRING

    override fun getMaxSourceSizeKBytes(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Long =
        LONG

    override fun isExplicitTransformation(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun transform(reader: ContentReader?, writer: ContentWriter?) {
        // deliberately omitted
    }

    override fun transform(reader: ContentReader?, writer: ContentWriter?, options: MutableMap<String, Any>?) {
        // deliberately omitted
    }

    override fun transform(reader: ContentReader?, contentWriter: ContentWriter?, options: TransformationOptions?) {
        // deliberately omitted
    }

    override fun isTransformable(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun isTransformable(sourceMimetype: String?, sourceSize: Long, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun getTransformationTime(): Long =
        LONG

    override fun getTransformationTime(sourceMimetype: String?, targetMimetype: String?): Long =
        LONG
}