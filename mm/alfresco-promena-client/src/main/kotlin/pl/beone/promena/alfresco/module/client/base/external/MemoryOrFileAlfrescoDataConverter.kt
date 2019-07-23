package pl.beone.promena.alfresco.module.client.base.external

import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.FileContentReader
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.Memory
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import java.net.URI

class MemoryOrFileAlfrescoDataConverter(private val externalCommunicationId: String,
                                        private val externalCommunicationLocation: URI? = null) : AlfrescoDataConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryOrFileAlfrescoDataConverter::class.java)
    }

    override fun createData(contentReader: ContentReader): Data =
        if (externalCommunicationId == File) {
            if (contentReader is FileContentReader) {
                FileData.of(contentReader.contentInputStream, externalCommunicationLocation!!)
            } else {
                logger.warn("Content reader type isn't FileContentReader (<{}>). Implementation <MemoryData> will be use as back pressure",
                            contentReader::class.java.simpleName)
                contentReader.toMemoryData()
            }
        } else if (externalCommunicationId == Memory) {
            contentReader.toMemoryData()
        } else {
            throw UnsupportedOperationException("External communication must be <$Memory> or <$File>")
        }

    override fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter) {
        contentWriter.putContent(data.getInputStream())

        deleteDataResource(data)
    }

    private fun ContentReader.toMemoryData(): MemoryData =
        contentInputStream.toMemoryData()

    // Trying to delete resource because it is possible that Promena returns different implementation than MemoryData or FileData
    private fun deleteDataResource(data: Data) {
        logger.debug("Deleting <{}> resource...", data.toSimplifiedString())
        try {
            data.delete()
        } catch (e: Exception) {
            when (e) {
                is UnsupportedOperationException,
                is DataDeleteException -> logger.debug("Couldn't delete <{}> resource", data.toSimplifiedString(), e)
                else                   -> throw e
            }
        }
        logger.debug("Finished deleting <{}> resource", data.toSimplifiedString())
    }

    private fun Data.toSimplifiedString(): String =
        try {
            "${this::class.java.simpleName}(location=${getLocation()})"
        } catch (e: Exception) {
            "${this::class.java.simpleName}(location=<isn't available>)"
        }
}
