@file:Suppress("DEPRECATION")

package pl.beone.promena.alfresco.module.rendition.mock.transformer

import org.alfresco.repo.content.transform.ContentTransformer
import org.alfresco.repo.content.transform.TransformerDebug
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.TransformationOptions
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.BOOLEAN
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.INT
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.NULL
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.STRING

class MockTransformerDebug : TransformerDebug(null, null, null, null, null, null) {

    override fun getFileName(options: TransformationOptions?, firstLevel: Boolean, sourceSize: Long): String =
        STRING

    override fun popIsTransformableSize() {
        // deliberately omitted
    }

    override fun debugTransformServiceRequest(
        sourceMimetype: String?,
        sourceSize: Long,
        sourceNodeRef: NodeRef?,
        contentHashcode: Int,
        fileName: String?,
        targetMimetype: String?,
        use: String?
    ): Int =
        INT

    override fun getTestFileExtensionsAndMimetypes(): Array<String> =
        emptyArray()

    override fun popTransform() {
        // deliberately omitted
    }

    override fun unavailableTransformer(transformer: ContentTransformer?, sourceMimetype: String?, targetMimetype: String?, maxSourceSizeKBytes: Long) {
        // deliberately omitted
    }

    override fun ms(time: Long): String =
        STRING

    override fun getSourceMimetypes(sourceExtension: String?): MutableCollection<String> =
        mutableListOf()

    override fun setContentService(contentService: ContentService?) {
        // deliberately omitted
    }

    override fun popAvailable() {
        // deliberately omitted
    }

    override fun sortTransformersByName(transformerName: String?): MutableCollection<ContentTransformer> =
        mutableListOf()

    override fun inactiveTransformer(transformer: ContentTransformer?) {
        // deliberately omitted
    }

    override fun popMisc() {
        // deliberately omitted
    }

    override fun debugTransformServiceResponse(
        sourceNodeRef: NodeRef?,
        contentHashcode: Int,
        requested: Long,
        seq: Int,
        sourceExt: String?,
        targetExt: String?,
        msg: String?
    ) {
        // deliberately omitted
    }

    override fun isEnabled(): Boolean =
        BOOLEAN

    override fun activeTransformer(
        mimetypePairCount: Int,
        transformer: ContentTransformer?,
        sourceMimetype: String?,
        targetMimetype: String?,
        maxSourceSizeKBytes: Long,
        firstMimetypePair: Boolean
    ) {
        // deliberately omitted
    }

    override fun activeTransformer(
        sourceMimetype: String?,
        targetMimetype: String?,
        transformerCount: Int,
        transformer: ContentTransformer?,
        maxSourceSizeKBytes: Long,
        firstTransformer: Boolean
    ) {
        // deliberately omitted
    }

    override fun transformationsByExtension(
        sourceExtension: String?,
        targetExtension: String?,
        toString: Boolean,
        format42: Boolean,
        onlyNonDeterministic: Boolean,
        use: String?
    ): String =
        STRING

    override fun getTargetMimetypes(
        sourceExtension: String?,
        targetExtension: String?,
        sourceMimetypes: MutableCollection<String>?
    ): MutableCollection<String> =
        mutableListOf()

    override fun pushMisc() {
        // deliberately omitted
    }

    override fun getStringBuilder(): StringBuilder? =
        NULL

    override fun testTransform(sourceExtension: String?, targetExtension: String?, use: String?): String =
        STRING

    override fun testTransform(transformerName: String?, sourceExtension: String?, targetExtension: String?, use: String?): String =
        STRING

    override fun transformationsByTransformer(transformerName: String?, toString: Boolean, format42: Boolean, use: String?): String =
        STRING

    override fun pushTransform(
        transformer: ContentTransformer?,
        fromUrl: String?,
        sourceMimetype: String?,
        targetMimetype: String?,
        sourceSize: Long,
        options: TransformationOptions?
    ) {
        // deliberately omitted
    }

    override fun blacklistTransform(transformer: ContentTransformer?, sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?) {
        // deliberately omitted
    }

    override fun pushIsTransformableSize(transformer: ContentTransformer?) {
        // deliberately omitted
    }

    override fun setStringBuilder(sb: StringBuilder?) {
        // deliberately omitted
    }

    override fun fileSize(size: Long): String =
        STRING

    override fun debug(message: String?) {
        // deliberately omitted
    }

    override fun debug(message: String?, t: Throwable?) {
        // deliberately omitted
    }

    override fun pushAvailable(fromUrl: String?, sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?) {
        // deliberately omitted
    }

    override fun <T : Throwable?> setCause(t: T): T? =
        NULL

    override fun availableTransformers(
        transformers: MutableList<ContentTransformer>?,
        sourceSize: Long,
        options: TransformationOptions?,
        calledFrom: String?
    ) {
        // deliberately omitted
    }
}