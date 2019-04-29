package pl.beone.promena.module.http.client.contract.alfresco

import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import pl.beone.promena.transformer.contract.model.Data

interface AlfrescoDataConverter {

    fun createData(contentReader: ContentReader): Data

    fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter)
}