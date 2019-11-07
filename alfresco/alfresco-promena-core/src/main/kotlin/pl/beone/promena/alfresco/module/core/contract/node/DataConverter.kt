package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import pl.beone.promena.transformer.contract.model.data.Data

interface DataConverter {

    fun createData(contentReader: ContentReader): Data

    fun saveDataInContentWriter(data: Data, contentWriter: ContentWriter)
}