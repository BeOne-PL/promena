package pl.beone.promena.intellij.plugin.saver

import pl.beone.promena.intellij.plugin.common.determineExtension
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.io.File

internal class TransformedDataDescriptorSaver {

    fun save(transformedDataDescriptor: TransformedDataDescriptor, mediaType: MediaType): List<File> {
        val extension = mediaType.determineExtension()

        val tempDir = createTempDir()
        return transformedDataDescriptor.descriptors
            .map { (data) ->
                saveFile(tempDir, data, extension)
                    .apply { deleteOnExit() }
            }
    }

    private fun saveFile(tempDir: File, data: Data, extension: String?): File =
        createTempFile(suffix = extension, directory = tempDir).apply {
            outputStream().use { data.getInputStream().copyTo(it) }
        }
}