package pl.beone.promena.alfresco.module.rendition.mock.transformer

import org.alfresco.repo.content.transform.ExplictTransformationDetails
import org.alfresco.repo.content.transform.RemoteTransformerClient
import org.alfresco.repo.content.transform.SupportedTransformation
import org.alfresco.repo.content.transform.TransformerConfig
import org.alfresco.repo.content.transform.magick.ImageMagickContentTransformerWorker
import org.alfresco.service.cmr.repository.*
import org.alfresco.util.exec.RuntimeExec
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.BOOLEAN
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.NULL
import pl.beone.promena.alfresco.module.rendition.mock.MockConstants.STRING
import java.io.File

class MockImageMagickContentTransformerWorker : ImageMagickContentTransformerWorker() {

    override fun setCheckCommand(checkCommand: RuntimeExec?) {
        // deliberately omitted
    }

    override fun getName(): String =
        STRING

    override fun getComments(available: Boolean): String =
        STRING

    override fun getCommentsOnlySupports(sourceMimetypes: MutableList<String>?, targetMimetypes: MutableList<String>?, available: Boolean): String =
        STRING

    override fun afterPropertiesSet() {
        // deliberately omitted
    }

    override fun isTransformable(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun setAvailable(available: Boolean) {
        // deliberately omitted
    }

    override fun setExplicitTransformations(explicitTransformations: MutableList<ExplictTransformationDetails>?) {
        // deliberately omitted
    }

    override fun register() {
        // deliberately omitted
    }

    override fun isExplicitTransformation(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun setTransformerConfig(transformerConfig: TransformerConfig?) {
        // deliberately omitted
    }

    override fun getImageMagickVersionNumber(): String =
        STRING

    override fun setUnsupportedTransformations(unsupportedTransformations: MutableList<SupportedTransformation>?) {
        // deliberately omitted
    }

    override fun transformRemote(
        reader: ContentReader?,
        writer: ContentWriter?,
        options: TransformationOptions?,
        sourceMimetype: String?,
        targetMimetype: String?,
        sourceExtension: String?,
        targetExtension: String?
    ) {
        // deliberately omitted
    }

    override fun setSupportedTransformations(supportedTransformations: MutableList<SupportedTransformation>?) {
        // deliberately omitted
    }

    override fun isAlphaOptionSupported(): Boolean =
        BOOLEAN

    override fun setRemoteTransformerClient(remoteTransformerClient: RemoteTransformerClient?) {
        // deliberately omitted
    }

    override fun deprecatedSetter(sourceMimetype: String?, targetMimetype: String?, suffixAndValue: String?) {
        // deliberately omitted
    }

    override fun remoteTransformerClientConfigured(): Boolean =
        BOOLEAN

    override fun transformInternal(
        sourceFile: File?,
        sourceMimetype: String?,
        targetFile: File?,
        targetMimetype: String?,
        options: TransformationOptions?
    ) {
        // deliberately omitted
    }

    override fun onlySupports(sourceMimetype: String?, targetMimetype: String?, available: Boolean): String =
        STRING

    override fun setMimetypeService(mimetypeService: MimetypeService?) {
        // deliberately omitted
    }

    override fun getBeanName(): String =
        STRING

    override fun getMimetypeService(): MimetypeService? =
        NULL

    override fun getMimetype(content: ContentAccessor?): String =
        STRING

    override fun getVersionString(): String =
        STRING

    override fun setBeanName(beanName: String) {
        // deliberately omitted
    }

    override fun isSupportedTransformation(sourceMimetype: String?, targetMimetype: String?, options: TransformationOptions?): Boolean =
        BOOLEAN

    override fun getExtensionOrAny(mimetype: String?): String =
        STRING

    override fun isAvailable(): Boolean =
        BOOLEAN

    override fun setExecuter(executer: RuntimeExec?) {
        // deliberately omitted
    }
}