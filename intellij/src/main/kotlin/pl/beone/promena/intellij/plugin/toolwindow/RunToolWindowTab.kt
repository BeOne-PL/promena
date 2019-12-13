package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import org.jetbrains.kotlin.idea.run.LocalFileHyperlinkInfo
import java.io.File
import javax.swing.Icon

internal class RunToolWindowTab(
    private val project: Project
) {

    private lateinit var toolWindow: ToolWindow
    private lateinit var content: Content
    private lateinit var consoleView: ConsoleView

    fun create(tabName: String) {
        initRunToolWindow()
        toolWindow = getToolWindow()
        consoleView = createConsoleView()

        content = createContent(tabName)
    }

    fun show() {
        toolWindow.contentManager.addContent(content)

        toolWindow.activate {
            toolWindow.contentManager.setSelectedContent(content, true)
        }
    }

    fun print(message: String, contentType: ConsoleViewContentType = ConsoleViewContentType.NORMAL_OUTPUT) {
        consoleView.print(message, contentType)
    }

    fun print(file: File) {
        consoleView.printHyperlink(file.path, LocalFileHyperlinkInfo(file.path, 0, 0))
    }

    fun println(message: String = "", contentType: ConsoleViewContentType = ConsoleViewContentType.NORMAL_OUTPUT) {
        print(message, contentType)
        consoleView.print("\n", contentType)
    }

    fun printlnError(message: String) {
        consoleView.print(message, ConsoleViewContentType.ERROR_OUTPUT)
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    fun scrollToTheBeginning() {
        flush()
        consoleView.scrollTo(0)
    }

    fun setIcon(icon: Icon) {
        content.icon = icon
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
    }

    fun getText(): String =
        (consoleView as ConsoleViewImpl).text

    fun flush() {
        (consoleView as ConsoleViewImpl).flushDeferredText()
    }

    fun onClose(toRun: () -> Unit) {
        content.setDisposer(toRun)
    }

    private fun initRunToolWindow() {
        ServiceManager.getService(project, RunContentManager::class.java)
    }

    private fun getToolWindow(): ToolWindow =
        ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.RUN)
            ?: error("Tool window <${ToolWindowId.RUN}> isn't available")

    private fun createConsoleView(): ConsoleView =
        TextConsoleBuilderFactory.getInstance()
            .createBuilder(project)
            .console

    private fun createContent(tabName: String): Content =
        toolWindow.contentManager.factory.createContent(consoleView.component, tabName, true)
}