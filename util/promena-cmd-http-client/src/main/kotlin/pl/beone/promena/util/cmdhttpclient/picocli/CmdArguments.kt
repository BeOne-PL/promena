package pl.beone.promena.util.cmdhttpclient.picocli

import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import java.io.File

class CmdArguments {

    @Option(names = ["--protocol"], defaultValue = "http")
    var protocol: String? = null

    @Option(names = ["-h", "--host"], required = true)
    var host: String? = null

    @Option(names = ["-p", "--port"], required = true)
    var port: Int? = null

    @Option(names = ["-t", "--timeout"], defaultValue = "60000")
    var timeout: Int? = null

    @Option(names = ["--max-connections"], defaultValue = "10")
    var maxConnections: Int? = null

    @Option(names = ["-ti", "--transformer-id"], required = true)
    var transformerId: String? = null

    @Option(names = ["-tmt", "--target-media-type"], converter = [MediaTypeConverter::class], required = true)
    var targetMediaType: MediaType? = null

    @Option(names = ["-param", "--parameters"])
    var parameters: String? = null

    @Option(names = ["--file-location"])
    var fileLocation: String? = null

    @Option(names = ["--threads"])
    var threads: Int? = null

    @Option(names = ["--repeat-times"])
    var repeatTimes: Int? = null

    @Parameters(arity = "1..*", converter = [FileConverter::class])
    var files: Array<File>? = null
}