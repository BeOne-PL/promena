package pl.beone.promena.util.cmdhttpclient

import org.apache.tika.Tika
import org.apache.tika.mime.MimeTypes
import org.apache.tika.utils.ExceptionUtils
import picocli.CommandLine
import pl.beone.promena.core.common.utils.measureTimeMillisWithContent
import pl.beone.promena.core.common.utils.toSeconds
import pl.beone.promena.core.common.utils.unwrapExecutionException
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.lib.http.client.contract.transformation.TransformationServerService
import pl.beone.promena.lib.http.client.external.httpclient.HttpTransformationExecutor
import pl.beone.promena.lib.http.client.internal.transformation.DefaultTransformationServerService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import pl.beone.promena.util.cmdhttpclient.picocli.CmdArguments
import java.io.File
import java.net.URI
import java.time.LocalDateTime
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    App(CommandLine.populateCommand(CmdArguments(), *args)).run()
}

class App(private val cmdArguments: CmdArguments) {

    fun run() {
        val location = determineLocation(cmdArguments.fileLocation)
        val dataDescriptors = cmdArguments.files!!.convertToDataDescriptors(location)
        val parameters = MapParameters.empty()

        val transformerId = cmdArguments.transformerId!!
        val targetMediaType = cmdArguments.targetMediaType!!

        val transformationExecutor = HttpTransformationExecutor(cmdArguments.protocol!!,
                                                                cmdArguments.host!!,
                                                                cmdArguments.port!!,
                                                                cmdArguments.timeout!!,
                                                                cmdArguments.maxConnections!!)

        val transformationServerService = DefaultTransformationServerService(transformationExecutor,
                                                                             KryoSerializationService(),
                                                                             cmdArguments.timeout!!.toLong(),
                                                                             location)

        val repeatTimes = cmdArguments.repeatTimes ?: 1
        val threads = cmdArguments.threads ?: 1

        println("Transforming using <$threads> threads. Repeating <$repeatTimes> times...")

        val executors = Executors.newFixedThreadPool(threads)

        val (successful, millis) = measureTimeMillisWithContent {
            (0 until repeatTimes).map {
                executors.submit {
                    transformationServerService.transform(transformerId,
                                                          targetMediaType,
                                                          dataDescriptors,
                                                          parameters)
                }
            }.map {
                try {
                    unwrapExecutionException { it.get() }
                    true
                } catch (e: Exception) {
                    println(ExceptionUtils.getStackTrace(e))
                    false
                }
            }.count { it }
        }

        println("Transformed in <${millis.toSeconds()} s>. Successful: <$successful>, failed: <${repeatTimes - successful}>")
    }

    private fun determineLocation(fileLocation: String?): URI? =
            if (fileLocation != null) {
                println("Communication: File, using <$fileLocation> location")
                URI(fileLocation)
            } else {
                println("Communication: Memory")
                null
            }

    private fun TransformationServerService.transform(transformerId: String,
                                                      targetMediaType: MediaType,
                                                      dataDescriptors: List<DataDescriptor>,
                                                      parameters: Parameters) {
        println("> Transforming <$transformerId> <$targetMediaType, ${parameters.getAll()}}> <${dataDescriptors.size} source(s)>: " +
                        "[${dataDescriptors.joinToString(", ") { "<${it.mediaType}>" }}]...")

        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            this.transform(transformerId, dataDescriptors, targetMediaType, parameters, null)
        }

        val files = transformedDataDescriptors.saveInFiles(targetMediaType)
        println("> Transformed <$transformerId> <${transformedDataDescriptors.size} result(s)> " +
                        "[${files.joinToString(", ") { "<${it.path}>" }}] in <${measuredTimeMs.toSeconds()} s>: " +
                        "[${transformedDataDescriptors.joinToString(", ") { "<${it.metadata}>" }}]")
    }

    private fun Array<File>.convertToDataDescriptors(location: URI?): List<DataDescriptor> =
            // memory communication
            if (location == null) {
                this.map { DataDescriptor(InMemoryData(it.readBytes()), it.getMimeType()) }
            } else { // file communication
                val locationDirectory = File(location)

                this.map { it.copyTo(createTempFile(directory = locationDirectory), true) }
                        .map { DataDescriptor(FileData(it.toURI()), it.getMimeType()) }
            }

    private fun List<TransformedDataDescriptor>.saveInFiles(targetMediaType: MediaType): List<File> {
        val now = LocalDateTime.now().toString()
        val extension = targetMediaType.getExtension()

        return this.withIndex().map { (index, transformedDataDescriptor) ->
            File("[$now] $index$extension").apply {
                writeBytes(transformedDataDescriptor.data.getBytes())
            }
        }
    }

    private fun File.getMimeType(): MediaType =
            MediaType.create(Tika().detect(this), Charsets.UTF_8)

    private fun MediaType.getExtension(): String =
            MimeTypes.getDefaultMimeTypes().forName(this.mimeType).extension
}
