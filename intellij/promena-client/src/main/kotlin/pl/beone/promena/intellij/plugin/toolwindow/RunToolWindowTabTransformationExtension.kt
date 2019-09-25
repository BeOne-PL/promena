package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.icons.AllIcons
import org.apache.commons.lang3.exception.ExceptionUtils
import pl.beone.promena.intellij.plugin.parser.datadescriptor.DataDescriptorWithFile
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.io.File

internal fun List<RunToolWindowTab>.logStart(tabName: String, httpAddress: String) {
    forEachIndexed { index, runToolWindowTab ->
        runToolWindowTab.create(tabName + " #${index + 1}")
        runToolWindowTab.setIcon(AllIcons.RunConfigurations.TestState.Run)
        runToolWindowTab.show()

        runToolWindowTab.println("Transforming using HTTP <$httpAddress>...")
    }
}

internal fun List<RunToolWindowTab>.logParameters(repeat: Int, concurrency: Int) {
    all {
        println("Parameters: <repeat: ${repeat}>, <concurrency: ${concurrency}>")
        scrollToTheBeginning()
    }
}

internal fun List<RunToolWindowTab>.logData(singleDataDescriptorWithFileList: List<DataDescriptorWithFile>) {
    all {
        println("Data descriptors <${singleDataDescriptorWithFileList.size}>:")
        singleDataDescriptorWithFileList.forEach { (dataDescriptor, file) ->
            print("> Data: <")
            print(file)
            print("; ${dataDescriptor.data.calculateSizeInMB()} MB> | MediaType: <${dataDescriptor.mediaType.mimeType}, ${dataDescriptor.mediaType.charset.name()}> | Metadata: <${emptyMetadata()}>")
            println()
        }
        scrollToTheBeginning()
    }
}

internal fun List<RunToolWindowTab>.newLine() {
    all {
        println()
    }
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
                "in <${calculateTimeInSeconds(executionTimeMillis)} s>:"
    )
    descriptors.zip(files).forEach { (transformedDataDescriptor, file) ->
        print("> Data: <")
        print(file)
        print("; ${transformedDataDescriptor.data.calculateSizeInMB()} MB> | Metadata: <${transformedDataDescriptor.metadata}>")
        println()
    }
    println("")
    scrollToTheBeginning()
}

internal fun List<RunToolWindowTab>.logFailureThrowable(exception: Throwable) {
    all {
        logFailureThrowable(exception)
    }
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

private fun calculateTimeInSeconds(millis: Long): String =
    String.format("%.3f", millis / 1000.0)

private fun List<RunToolWindowTab>.all(block: RunToolWindowTab.() -> Unit) {
    forEach {
        it.apply(block)
    }
}