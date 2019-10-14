package pl.beone.promena.alfresco.module.core.external

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.FileContentReader
import pl.beone.promena.alfresco.module.core.contract.DataConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import java.io.File

class MemoryOrFileDataConverter(
    private val externalCommunicationId: String,
    private val externalCommunicationDirectory: File? = null
) : DataConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun createData(contentReader: ContentReader): Data =
        if (externalCommunicationId == FileCommunicationParameters.ID) {
            if (contentReader is FileContentReader) {
                FileData.of(contentReader.contentInputStream, externalCommunicationDirectory!!)
            } else {
                logger.warn { "Content reader type isn't FileContentReader (<${contentReader::class.java.simpleName}>). Implementation <MemoryData> will be use as back pressure" }
                contentReader.toMemoryData()
            }
        } else if (externalCommunicationId == MemoryCommunicationParameters.ID) {
            contentReader.toMemoryData()
        } else {
            throw UnsupportedOperationException("External communication must be <${MemoryCommunicationParameters.ID}> or <${FileCommunicationParameters.ID}>")
        }

    override fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter) {
        contentWriter.putContent(data.getInputStream())

        deleteDataResource(data)
    }

    private fun ContentReader.toMemoryData(): MemoryData =
        contentInputStream.toMemoryData()

    // Trying to delete resource because it is possible that Promena returns different implementation than MemoryData or FileData
    private fun deleteDataResource(data: Data) {
        logger.debug { "Deleting <${data.toSimplifiedString()}> resource..." }
        try {
            data.delete()
        } catch (e: Exception) {
            when (e) {
                is UnsupportedOperationException,
                is DataDeleteException -> logger.debug(e) { "Couldn't delete <${data.toSimplifiedString()}> resource" }
                else                   -> throw e
            }
        }
        logger.debug { "Finished deleting <${data.toSimplifiedString()}> resource" }
    }

    private fun Data.toSimplifiedString(): String =
        try {
            "${this::class.java.simpleName}(location=${getLocation()})"
        } catch (e: Exception) {
            "${this::class.java.simpleName}(location=<isn't available>)"
        }
}
