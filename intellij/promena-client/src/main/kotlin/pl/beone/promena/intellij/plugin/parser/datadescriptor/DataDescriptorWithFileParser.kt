package pl.beone.promena.intellij.plugin.parser.datadescriptor

import pl.beone.promena.intellij.plugin.common.detectCharset
import pl.beone.promena.intellij.plugin.common.detectMimeType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.io.File
import java.net.URL
import java.nio.charset.Charset

private data class CommentDataDescriptor(
    val dataPath: String,
    val mimeType: String?,
    val charset: String?
)

internal class DataDescriptorParser {

    companion object {
        private val dataRegex = "Data:(.*)".toRegex()
        private val dataWithMediaTypeRegex = "$dataRegex[|][ ]+MediaType:(.*)".toRegex()
        private val dataWithMediaTypeAndCharsetRegex = "$dataWithMediaTypeRegex;(.*)".toRegex()
    }

    fun parse(comments: List<String>, clazz: Class<*>): List<DataDescriptorWithFile> =
        comments.filter { it.contains("Data") }
            .map(::createCommentDataDescriptor)
            .map { createMemoryDataDescriptor(it, clazz) }

    private fun createCommentDataDescriptor(comment: String): CommentDataDescriptor =
        determineDataDescriptorWithMediaTypeAndCharset(comment) ?: determineDataDescriptorWithMediaType(comment) ?: determineData(comment)
        ?: throw IllegalArgumentException(
            "Couldn't parse <$comment>. Correct format: " +
                    "// Data: <absolute/resource path> [| MediaType: <mime type>; <charset>], " +
                    "for example: // Data: <absolute/resource path> [| MediaType: <mime type>; <charset>]"
        )

    private fun determineDataDescriptorWithMediaTypeAndCharset(comment: String): CommentDataDescriptor? =
        dataWithMediaTypeAndCharsetRegex.find(comment)?.let {
            val (dataPath, mimeType, charset) = it.destructured
            CommentDataDescriptor(dataPath.trim(), mimeType.trim(), charset.trim())
        }

    private fun determineDataDescriptorWithMediaType(comment: String): CommentDataDescriptor? =
        dataWithMediaTypeRegex.find(comment)?.let {
            val (dataPath, mimeType) = it.destructured
            CommentDataDescriptor(dataPath.trim(), mimeType.trim(), null)
        }

    private fun determineData(comment: String): CommentDataDescriptor? =
        dataRegex.find(comment)?.let {
            val (dataPath) = it.destructured
            CommentDataDescriptor(dataPath.trim(), null, null)
        }

    private fun createMemoryDataDescriptor(commentDataDescriptor: CommentDataDescriptor, clazz: Class<*>): DataDescriptorWithFile =
        try {
            val file = determineFile(commentDataDescriptor.dataPath, clazz)
            DataDescriptorWithFile(
                singleDataDescriptor(
                    file.readBytes().toMemoryData(),
                    createMediaType(file, commentDataDescriptor.mimeType, commentDataDescriptor.charset),
                    emptyMetadata()
                ),
                file
            )
        } catch (e: Exception) {
            throw RuntimeException("Couldn't create DataDescriptor from <$commentDataDescriptor>", e)
        }

    private fun determineFile(dataPath: String, clazz: Class<*>): File =
        if (dataPath.isAbsolutePath()) {
            File(dataPath)
        } else {
            File(getResourceUrl(dataPath, clazz).toURI())
        }

    private fun String.isAbsolutePath(): Boolean =
        this.startsWith("/")

    private fun createMediaType(file: File, mimeType: String?, charset: String?): MediaType =
        when {
            mimeType != null && charset != null -> mediaType(mimeType, Charset.forName(charset))
            mimeType != null                    -> mediaType(mimeType, file.detectCharset())
            else                                -> mediaType(file.detectMimeType(), file.detectCharset())
        }

    private fun getResourceUrl(path: String, clazz: Class<*>): URL =
        clazz.getResource("/$path") ?: throw IllegalArgumentException("Couldn't get <$path> from resources")
}