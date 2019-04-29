package pl.beone.promena.util.cmdhttpclient.picocli

import picocli.CommandLine
import java.io.File

class FileConverter : CommandLine.ITypeConverter<File> {

    override fun convert(value: String): File {
        val file = File(value)
        if (!file.exists()) {
            throw Exception("File <${file.absolutePath}> doesn't exist")
        }
        return file
    }
}