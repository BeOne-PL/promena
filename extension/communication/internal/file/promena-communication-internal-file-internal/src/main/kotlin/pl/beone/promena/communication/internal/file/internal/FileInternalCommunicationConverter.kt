package pl.beone.promena.communication.internal.file.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import java.io.File
import java.net.URI

class FileInternalCommunicationConverter(internalCommunicationParameters: CommunicationParameters) : InternalCommunicationConverter {

    private val internalCommunicationLocation = internalCommunicationParameters.get("location", URI::class.java)

    companion object {
        private val logger = LoggerFactory.getLogger(FileInternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptors: List<DataDescriptor>,
                         transformedDataDescriptors: List<TransformedDataDescriptor>): List<TransformedDataDescriptor> {
        val convertedTransformedDataDescriptors = transformedDataDescriptors.map { convertIfNecessary(it) }
        removeFilesThatAreNotLongerUse(dataDescriptors.map { it.data }, transformedDataDescriptors.map { it.data })
        return convertedTransformedDataDescriptors
    }

    private fun convertIfNecessary(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor {
        val data = transformedDataDescriptor.data
        val metadata = transformedDataDescriptor.metadata

        return when {
            data !is FileData                                                -> {
                logger.warn("One of transformed data in internal communication is type <{}> but should be <FileData>. You should use the same data implementation for performance reasons",
                            data::class.java.simpleName)
                TransformedDataDescriptor(data.createFileDataAndDeleteOldDataResource(), metadata)
            }
            data.getLocation().isNotSubPathOf(internalCommunicationLocation) -> {
                logger.warn("Both transformed data are <FileData> but the file <{}> isn't included in internal communication location <{}>. You should choose the same communication parameters for performance reasons",
                            data.getLocation(), internalCommunicationLocation)

                TransformedDataDescriptor(data.moveFileAndCreateNewFileData(), metadata)
            }
            else                                                             ->
                transformedDataDescriptor
        }
    }

    private fun Data.createFileDataAndDeleteOldDataResource(): FileData {
        val file = internalCommunicationLocation.createFile()
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

    private fun Data.moveFileAndCreateNewFileData(): Data {
        val file = File(getLocation())
        val newFile = internalCommunicationLocation.createFile()

        logger.debug("Moving file from <{}> to <{}>...", file.path, newFile.path)
        file.renameTo(newFile)
        logger.debug("Finished moving file from <{}> to <{}>", file.path, newFile.path)

        return newFile.toFileData()
    }

    private fun File.toFileData(): FileData =
            FileData(toURI())

    private fun removeFilesThatAreNotLongerUse(data: List<Data>, transformedData: List<Data>) {
        val noLongerUseFiles = data.getFileLocations() - transformedData.getFileLocations()

        logger.debug("<{}> files no longer use", noLongerUseFiles.size)

        noLongerUseFiles.forEach { it.delete() }
    }

    private fun List<Data>.getFileLocations(): List<URI> =
            filterIsInstance(FileData::class.java)
                    .map { it.getLocation() }

    private fun URI.delete() {
        logger.debug("Deleting <{}>...", this)
        val deleted = File(this).delete()

        if (deleted) {
            logger.debug("Finished deleting <{}>", this)
        } else {
            logger.warn("Couldn't delete <{}>", this)
        }
    }

}

