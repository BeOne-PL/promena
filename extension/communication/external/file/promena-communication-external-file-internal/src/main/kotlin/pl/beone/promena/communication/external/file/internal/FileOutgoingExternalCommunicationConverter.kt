package pl.beone.promena.communication.external.file.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import java.io.File
import java.net.URI

class FileOutgoingExternalCommunicationConverter(private val internalCommunicationParameters: CommunicationParameters) : OutgoingExternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(FileOutgoingExternalCommunicationConverter::class.java)
    }

    override fun convert(transformedDataDescriptors: List<TransformedDataDescriptor>,
                         externalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor> {
        val externalCommunicationLocation = externalCommunicationParameters.getLocation()

        val externalCommunicationId = externalCommunicationParameters.getId()
        val internalCommunicationId = internalCommunicationParameters.getId()

        return when {
            externalCommunicationId != internalCommunicationId                                          -> {
                logger.warn("External communication is <{}> but internal communication is <{}>. You should choose the same communication implementation for performance reasons",
                            externalCommunicationId, internalCommunicationId)

                transformedDataDescriptors.map {
                    TransformedDataDescriptor(it.data.createFileDataAndDeleteOldDataResource(externalCommunicationLocation), it.metadata)
                }
            }

            externalCommunicationLocation.isNotSubPathOf(internalCommunicationParameters.getLocation()) -> {
                logger.warn("Both communications are <file> but they have different locations (<{}> and <{}>). You should choose the same communication parameters for performance reasons",
                            externalCommunicationLocation, internalCommunicationParameters.getLocation())

                transformedDataDescriptors.map {
                    TransformedDataDescriptor(it.data.moveFileAndCreateNewFileData(externalCommunicationLocation), it.metadata)
                }
            }

            else                                                                                        ->
                transformedDataDescriptors
        }
    }

    private fun CommunicationParameters.getLocation(): URI =
            get("location", URI::class.java)

    private fun Data.createFileDataAndDeleteOldDataResource(location: URI): FileData {
        val file = location.createFile()
        logger.debug("Creating <FileData> in <{}> from <{}>...", file.path, this.toSimplifiedString())
        val fileData = this.toFileData(file)
        logger.debug("Finished creating <FileData> in <{}> from <{}>", file.path, this.toSimplifiedString())

        logger.debug("Deleting <{}> resource...", this.toSimplifiedString())
        try {
            this.delete()
        } catch (e: Exception) {
            when (e) {
                is UnsupportedOperationException,
                is DataDeleteException -> logger.debug("Couldn't delete <{}> resource", this.toSimplifiedString(), e)
                else                   -> throw e
            }
        }
        logger.debug("Finished deleting <{}> resource", this.toSimplifiedString())

        return fileData
    }

    private fun URI.createFile(): File =
            createTempFile(directory = File(this))

    private fun Data.toSimplifiedString(): String =
            try {
                "${this::class.java.simpleName}(location=${getLocation()})"
            } catch (e: Exception) {
                "${this::class.java.simpleName}(location=<isn't available>)"
            }

    private fun Data.toFileData(file: File): FileData =
            FileData(file.apply {
                writeBytes(getBytes())
            }.toURI())

    private fun URI.includes(location: URI): Boolean =
            this.toASCIIString().startsWith(location.toASCIIString())

    private fun URI.isNotSubPathOf(location: URI): Boolean =
            !includes(location)

    private fun Data.moveFileAndCreateNewFileData(location: URI): Data {
        val file = File(getLocation())
        val newFile = location.createFile()

        logger.debug("Moving file from <{}> to <{}>...", file.path, newFile.path)
        file.renameTo(newFile)
        logger.debug("Finished moving file from <{}> to <{}>", file.path, newFile.path)

        return newFile.toFileData()
    }

    private fun File.toFileData(): FileData =
            FileData(toURI())

}
