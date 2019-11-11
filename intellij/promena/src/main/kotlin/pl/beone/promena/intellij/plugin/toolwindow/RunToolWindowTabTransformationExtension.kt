package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons
import pl.beone.promena.intellij.plugin.parser.DataDescriptorWithFile
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.io.File

internal fun RunToolWindowTab.logStart(tabName: String, address: String) {
    create(tabName)
    setIcon(AllIcons.RunConfigurations.TestState.Run)
    show()

    println("Executing on Promena <$address>...")
    println()
}

internal fun RunToolWindowTab.newLine() {
    println()
}

internal fun RunToolWindowTab.logData(singleDataDescriptorWithFileList: List<DataDescriptorWithFile>) {
    println("Data descriptors <${singleDataDescriptorWithFileList.size}>:")
    singleDataDescriptorWithFileList.forEach { (dataDescriptor, file) ->
        print("> Data: <")
        print(file)
        print("; ${dataDescriptor.data.calculateSizeInMB()} MB> | MediaType: <${dataDescriptor.mediaType.mimeType}, ${dataDescriptor.mediaType.charset.name()}> | Metadata: <${emptyMetadata()}>")
        println()
    }
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logSuccess(
    transformedDataDescriptor: TransformedDataDescriptor,
    targetMediaType: MediaType,
    files: List<File>,
    executionTimeMillis: Long
) {
    setIcon(AllIcons.RunConfigurations.TestPassed)

    val descriptors = transformedDataDescriptor.descriptors
    println(
        "Transformed <${targetMediaType.mimeType}, ${targetMediaType.charset.name()}> data descriptors <${descriptors.size}> " +
                "in <${calculateTimeInSeconds(executionTimeMillis)} s>:",
        ConsoleViewContentType.USER_INPUT
    )
    descriptors.zip(files).forEach { (transformedDataDescriptor, file) ->
        print("> Data: <", ConsoleViewContentType.USER_INPUT)
        print(file)
        print(
            "; ${transformedDataDescriptor.data.calculateSizeInMB()} MB> | Metadata: <${transformedDataDescriptor.metadata}>",
            ConsoleViewContentType.USER_INPUT
        )
        println()
    }
    println("")
    scrollToTheBeginning()
}

internal fun RunToolWindowTab.logFailureThrowable(exceptionString: String) {
    setIcon(AllIcons.RunConfigurations.TestError)
    if (notEndsWithDoubleNewLine()) {
        println()
    }
    printlnError(exceptionString)
    scrollToTheBeginning()
}

private fun RunToolWindowTab.notEndsWithDoubleNewLine(): Boolean {
    flush()
    return !getText().endsWith("\n\n")
}

private fun Data.calculateSizeInMB(): String =
    getBytes().toMB().format(2)

private fun ByteArray.toMB(): Double =
    this.size.toDouble() / 1024 / 1024

private fun Double.format(digits: Int): String =
    String.format("%.${digits}f", this)

private fun calculateTimeInSeconds(millis: Long): String =
    String.format("%.3f", millis / 1000.0)