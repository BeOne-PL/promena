package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.icons.AllIcons
import pl.beone.promena.intellij.plugin.parser.Parameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import java.io.PrintWriter
import java.io.StringWriter

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
        println("> Data: <${it.data.getBytes().toMB().format(2)} MB> | MediaType: <${it.mediaType.mimeType}, ${it.mediaType.charset.name()}>")
    }
    println()
}

internal fun RunToolWindowTab.logSuccess() {
    setIcon(AllIcons.RunConfigurations.TestPassed)
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
    StringWriter().apply {
        printStackTrace(PrintWriter(this))
    }.toString()

private fun ByteArray.toMB(): Double =
    this.size.toDouble() / 1024 / 1024

private fun Double.format(digits: Int): String =
    String.format("%.${digits}f", this)

//    private fun Duration.prettyPrint(): String =
//        this.toString()
//            .substring(2)
//            .replace("(\\d[HMS])(?!$)", "$1 ")
//            .toLowerCase()