package pl.beone.promena.alfresco.module.client.base.external

import com.google.common.io.ByteStreams
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.FileContentReader
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.Memory
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI

class InMemoryOrFileAlfrescoDataConverter(private val externalCommunicationId: String,
                                          private val externalCommunicationLocation: URI? = null) : AlfrescoDataConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(InMemoryOrFileAlfrescoDataConverter::class.java)
    }

    override fun createData(contentReader: ContentReader): Data =
            if (externalCommunicationId == File) {
                if (contentReader is FileContentReader) {
                    FileData(
                            createTmpFileInLocation().apply {
                                contentReader.file.copyTo(this, true)
                            }.toURI()
                    )
                } else {
                    logger.warn("Content reader type isn't FileContentReader (<{}>). Implementation <InMemoryData> will be use as back pressure",
                                contentReader::class.java.name)
                    contentReader.toInMemoryData()
                }
            } else if (externalCommunicationId == Memory) {
                contentReader.toInMemoryData()
            } else {
                throw UnsupportedOperationException("External communication has to be <$Memory> or <$File>")
            }

    // TODO maybe copy directly?
    override fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter) {
        data.getBytes().save(contentWriter)
    }

    private fun createTmpFileInLocation(): File = createTempFile(directory = File(externalCommunicationLocation!!))

    //  TODO verify unstability of this method
    private fun ContentReader.getBytes(): ByteArray = ByteStreams.toByteArray(this.contentInputStream)

    private fun ContentReader.toInMemoryData(): InMemoryData = InMemoryData(this.getBytes())

    private fun ByteArray.save(contentWriter: ContentWriter) {
        contentWriter.putContent(ByteArrayInputStream(this))
    }
}
