package pl.beone.promena.alfresco.module.rendition.mock.transformer

import org.alfresco.repo.content.transform.ContentTransformer
import org.alfresco.repo.content.transform.ContentTransformerRegistry
import org.alfresco.repo.content.transform.TransformerDebug
import org.alfresco.service.cmr.repository.TransformationOptions
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.NULL

class MockContentTransformerRegistry : ContentTransformerRegistry(null) {

    override fun getActiveTransformers(
        sourceMimetype: String?,
        sourceSize: Long,
        targetMimetype: String?,
        options: TransformationOptions?
    ): MutableList<ContentTransformer> =
        mutableListOf()

    override fun removeTransformer(transformer: ContentTransformer?) {
        // deliberately omitted
    }

    override fun getTransformers(): MutableList<ContentTransformer> =
        mutableListOf()

    override fun getAllTransformers(): MutableList<ContentTransformer> =
        mutableListOf()

    override fun addTransformer(transformer: ContentTransformer?) {
        // deliberately omitted
    }

    override fun setEnabled(enabled: Boolean) {
        // deliberately omitted
    }

    override fun setTransformerDebug(transformerDebug: TransformerDebug?) {
        // deliberately omitted
    }

    override fun getTransformer(transformerName: String?): ContentTransformer? =
        NULL

    override fun getTransformer(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): ContentTransformer? =
        NULL

    override fun getTransformer(sourceMimetype: String?, sourceSize: Long, targetMimetype: String?, options: TransformationOptions?): ContentTransformer? =
        NULL

    override fun addComponentTransformer(transformer: ContentTransformer?) {
        // deliberately omitted
    }
}