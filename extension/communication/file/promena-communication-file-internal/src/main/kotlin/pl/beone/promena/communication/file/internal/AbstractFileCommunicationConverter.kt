package pl.beone.promena.communication.file.internal

import org.slf4j.Logger
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationException
import pl.beone.promena.transformer.contract.descriptor.AbstractDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import java.io.File
import java.net.URI

abstract class AbstractFileCommunicationConverter<T : AbstractDescriptor> {

    protected abstract fun logger(): Logger

    protected abstract fun convertDescriptor(currentDescriptor: T, data: Data): T

    protected fun convertImpl(descriptor: T, location: URI, tryToSaveInFile: Boolean): T {
        val data = descriptor.data

        if (location.scheme != "file") {
            throw CommunicationException("Location <$location> hasn't <file> scheme")
        }

        val dataLocation = try {
            data.getLocation()
        } catch (e: UnsupportedOperationException) {
            if (tryToSaveInFile) {
                logger().warn("Data exists only in memory. Saving data in file...")

                return descriptor.saveBytesInFileAndConvert(location)
            } else {
                throw CommunicationException("Data exists only in memory but should be file", e)
            }
        }

        if (dataLocation.scheme != "file") {
            if (tryToSaveInFile) {
                logger().warn("Data location <$dataLocation> hasn't <file> scheme. Saving data in file...")

                return descriptor.saveBytesInFileAndConvert(location)
            } else {
                throw CommunicationException("Data location <$dataLocation> hasn't <file> scheme")
            }
        }

        return if (dataLocation.isContainedIn(location)) {
            logger().debug("File location <{}> is contained in communication location <{}>. No need to copy", dataLocation, location)

            convertDescriptor(descriptor, descriptor.data)
        } else {
            logger().warn("File location <{}> isn't contained in communication location <{}>. Copying file...", dataLocation, location)

            convertDescriptor(descriptor, FileData(descriptor.copyFileToLocation(location).toURI()))
        }
    }

    private fun T.saveBytesInFileAndConvert(location: URI): T {
        val file = createTmpFileInLocation(location).apply {
            writeBytes(data.getBytes())
        }

        return convertDescriptor(this, FileData(file.toURI()))
    }

    private fun T.copyFileToLocation(location: URI): File {
        val file = createTmpFileInLocation(location)

        return File(data.getLocation()).copyTo(file, true)
    }

    private fun createTmpFileInLocation(location: URI): File =
            createTempFile(directory = File(location))

    private fun URI.isContainedIn(location: URI): Boolean =
            this.toASCIIString().contains(location.toASCIIString())

}

