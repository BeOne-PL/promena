package pl.beone.promena.alfresco.module.core.external.node

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.FileContentReader
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.FileData
import pl.beone.promena.transformer.internal.model.data.memory.MemoryData
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import java.io.File

class MemoryOrFileDataConverter(
    private val externalCommunicationId: String,
    private val externalCommunicationDirectory: File? = null
) : DataConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun createData(contentReader: ContentReader): Data =
        if (externalCommunicationId == FileCommunicationParametersConstants.ID) {
            if (contentReader is FileContentReader) {
                FileData.of(contentReader.contentInputStream, externalCommunicationDirectory!!)
            } else {
                logger.warn { "Content reader type isn't FileContentReader (<${contentReader::class.java.simpleName}>). Implementation <MemoryData> will be use as back pressure" }
                contentReader.toMemoryData()
            }
        } else if (externalCommunicationId == MemoryCommunicationParametersConstants.ID) {
            contentReader.toMemoryData()
        } else {
            throw UnsupportedOperationException("External communication must be <${MemoryCommunicationParametersConstants.ID}> or <${FileCommunicationParametersConstants.ID}>")
        }

    override fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter) {
        contentWriter.putContent(data.getInputStream())
    }

    private fun ContentReader.toMemoryData(): MemoryData =
        contentInputStream.toMemoryData()
}
