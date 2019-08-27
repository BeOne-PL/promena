package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.icons.AllIcons
import org.apache.commons.lang3.exception.ExceptionUtils
import pl.beone.promena.intellij.plugin.parser.Parameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.io.File

internal fun RunToolWindowTab.logStart(tabName: String) {
    create(tabName)
    setIcon(AllIcons.RunConfigurations.TestState.Run)
    show()

    println("Transforming...")
}

internal fun RunToolWindowTab.logParameters(parameters: Parameters) {
    println("Parameters: <repeat: ${parameters.repeat}>, <concurrency: ${parameters.concurrency}>")
}

internal fun RunToolWindowTab.logData(dataDescriptor: DataDescriptor) {
    val descriptors = dataDescriptor.descriptors
    println("Data descriptors <${descriptors.size}>:")
    descriptors.forEach {
        println("> Data: <${it.data.getBytes().toMB().format(2)} MB> | MediaType: <${it.mediaType.mimeType}, ${it.mediaType.charset.name()}> | Metadata: <${emptyMetadata()}>")
    }
    println()
}

internal fun RunToolWindowTab.logSuccess(transformedDataDescriptor: TransformedDataDescriptor, targetMediaType: MediaType, savedFiles: List<File>) {
    setIcon(AllIcons.RunConfigurations.TestPassed)

    val descriptors = transformedDataDescriptor.descriptors
    println("Transformed <${targetMediaType.mimeType}, ${targetMediaType.charset.name()}> data descriptors <${descriptors.size}>:")
    descriptors.zip(savedFiles).forEach { (transformedDataDescriptor, savedFile) ->
        println("> Data: <${savedFile.path}, ${transformedDataDescriptor.data.getBytes().toMB().format(2)} MB> | Metadata: <${transformedDataDescriptor.metadata}>")
    }
}

internal fun RunToolWindowTab.logFailureCompilationError() {
    setIcon(AllIcons.RunConfigurations.TestError)
    printlnError("Compilation failed. Check messages for more details.")
}

internal fun RunToolWindowTab.logFailureException(exception: Throwable) {
    setIcon(AllIcons.RunConfigurations.TestError)
    printlnError(exception.toFullString())
}

private fun Throwable.toFullString(): String =
    ExceptionUtils.getStackTrace(this)

private fun ByteArray.toMB(): Double =
    this.size.toDouble() / 1024 / 1024

private fun Double.format(digits: Int): String =
    String.format("%.${digits}f", this)

//    private fun Duration.prettyPrint(): String =
//        this.toString()
//            .substring(2)
//            .replace("(\\d[HMS])(?!$)", "$1 ")
//            .toLowerCase()