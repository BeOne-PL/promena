package pl.beone.transformation.module.service.external.alfresco.content

import com.google.common.io.ByteStreams
import org.alfresco.repo.content.ContentServiceImpl
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.TransformationOptions
import org.slf4j.LoggerFactory
import pl.beone.transformation.module.client.applicationmodel.TransformationType
import pl.beone.transformation.module.client.applicationmodel.exception.TransformationException
import pl.beone.transformation.module.client.external.httpclient.RestTransformationService
import pl.beone.transformation.server.applicationmodel.mimetype.MimeType
import pl.beone.transformation.server.applicationmodel.parameters.Parameters
import java.io.ByteArrayInputStream

class TransformationContentService(private val restTransformationService: RestTransformationService) : ContentServiceImpl() {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformationContentService::class.java)
    }

    override fun transform(reader: ContentReader, writer: ContentWriter, options: TransformationOptions) {
        try {
            val convertedByteArray =
                    transform(reader.readContent(), reader.encoding, reader.mimetype.convertToMimeType(), writer.mimetype.convertToMimeType())

            writer.writeContent(convertedByteArray)
        } catch (e: Exception) {
            logger.error("Error occurred during making transformation from <{}> to <{}>", reader, writer, e)
        }
    }

    private fun transform(byteArray: ByteArray,
                          charset: String,
                          mimeType: MimeType,
                          targetMimeType: MimeType): ByteArray {
        val contentWithMetadataList =
                restTransformationService.transform(TransformationType.CONVERT,
                                                    byteArray,
                                                    charset,
                                                    mimeType,
                                                    targetMimeType,
                                                    Parameters.empty(),
                                                    null)

        if (contentWithMetadataList.size > 1) {
            throw TransformationException("Transformation returned more than one document <${contentWithMetadataList.size}>")
        }

        return contentWithMetadataList[0].bytes
    }

    private fun ContentReader.readContent() =
            ByteStreams.toByteArray(this.contentInputStream)

    private fun ContentWriter.writeContent(byteArray: ByteArray) =
            this.putContent(ByteArrayInputStream(byteArray))

    private fun String.convertToMimeType(): MimeType =
            MimeType.convertToMimeType(this)
}