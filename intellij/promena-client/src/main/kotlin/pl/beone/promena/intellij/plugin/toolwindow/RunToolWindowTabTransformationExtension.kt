package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.icons.AllIcons
import org.apache.commons.lang3.exception.ExceptionUtils
import pl.beone.promena.intellij.plugin.parser.datadescriptor.DataDescriptorWithFile
import pl.beone.promena.intellij.plugin.parser.parameter.Parameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
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
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logData(singleDataDescriptorWithFileList: List<DataDescriptorWithFile>) {
    println("Data descriptors <${singleDataDescriptorWithFileList.size}>:")
    singleDataDescriptorWithFileList.forEach { (dataDescriptor, file) ->
        print("> Data: <")
        print(file)
        print(", ${dataDescriptor.data.calculateSizeInMB()} MB> | MediaType: <${dataDescriptor.mediaType.mimeType}, ${dataDescriptor.mediaType.charset.name()}> | Metadata: <${emptyMetadata()}>")
        println()
    }
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logSuccess(transformedDataDescriptor: TransformedDataDescriptor, targetMediaType: MediaType, files: List<File>) {
    setIcon(AllIcons.RunConfigurations.TestPassed)

    val descriptors = transformedDataDescriptor.descriptors
    println("Transformed <${targetMediaType.mimeType}, ${targetMediaType.charset.name()}> data descriptors <${descriptors.size}>:")
    descriptors.zip(files).forEach { (transformedDataDescriptor, file) ->
        print("> Data: <")
        print(file)
        print(", ${transformedDataDescriptor.data.calculateSizeInMB()} MB> | Metadata: <${transformedDataDescriptor.metadata}>")
        println()
    }
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logFailureCompilationError() {
    setIcon(AllIcons.RunConfigurations.TestError)
    printlnError("Compilation failed. Check messages for more details.")
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logFailureThrowable(exception: Throwable) {
    setIcon(AllIcons.RunConfigurations.TestError)
    if (notEndsWithDoubleNewLine()) {
        println()
    }
    printlnError(exception.toFullString())
    scrollToTheBeginning()
}

private fun RunToolWindowTab.notEndsWithDoubleNewLine(): Boolean {
    flush()
    return !getText().endsWith("\n\n")
}

private fun Throwable.toFullString(): String =
    ExceptionUtils.getStackTrace(this)

private fun Data.calculateSizeInMB(): String =
    getBytes().toMB().format(2)

private fun ByteArray.toMB(): Double =
    this.size.toDouble() / 1024 / 1024

private fun Double.format(digits: Int): String =
    String.format("%.${digits}f", this)

//    private fun Duration.prettyPrint(): String =
//        this.toString()
//            .substring(2)
//            .replace("(\\d[HMS])(?!$)", "$1 ")
//            .toLowerCase()