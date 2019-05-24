package pl.beone.promena.alfresco.module.client.messagebroker.external

import com.google.common.io.ByteStreams
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.FileContentReader
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataConverter
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI

class FileAlfrescoDataConverter(private val communicationLocation: URI?) : AlfrescoDataConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(FileAlfrescoDataConverter::class.java)
    }

    override fun createData(contentReader: ContentReader): Data =
            if (communicationLocation != null) {
                if (contentReader is FileContentReader) {
                    val tempFile = createTmpFileInLocation()
                    contentReader.file.copyTo(tempFile, true)
                    FileData(tempFile.toURI())
                } else {
                    logger.warn("You choose communication based on files but this content reader isn't FileContentReader (<{}>). Loading into memory...",
                                contentReader::class.java.name)
                    contentReader.toInMemoryData()
                }
            } else {
                contentReader.toInMemoryData()
            }

    override fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter) {
        data.getBytes().save(contentWriter)
    }

    private fun createTmpFileInLocation(): File = createTempFile(directory = File(communicationLocation))

    private fun ContentReader.getBytes(): ByteArray = ByteStreams.toByteArray(this.contentInputStream)

    private fun ContentReader.toInMemoryData(): InMemoryData = InMemoryData(this.getBytes())

    private fun ByteArray.save(contentWriter: ContentWriter) {
        contentWriter.putContent(ByteArrayInputStream(this))
    }
}
