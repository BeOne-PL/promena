package pl.beone.promena.intellij.plugin.toolwindow

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import javax.swing.Icon

internal class RunToolWindowTab(private val project: Project) {

    private lateinit var toolWindow: ToolWindow
    private lateinit var consoleView: ConsoleView
    private lateinit var content: Content

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

    fun println(message: String = "") {
        consoleView.print(message, ConsoleViewContentType.NORMAL_OUTPUT)
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    fun printlnError(message: String) {
        consoleView.print(message, ConsoleViewContentType.ERROR_OUTPUT)
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    fun setIcon(icon: Icon) {
        content.icon = icon
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
    }

    private fun initRunToolWindow() {
        ServiceManager.getService(project, RunContentManager::class.java)
    }

    private fun getToolWindow(): ToolWindow =
        ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.RUN)
            ?: throw IllegalStateException("Tool window <${ToolWindowId.RUN}> isn't available")

    private fun createConsoleView(): ConsoleView =
        TextConsoleBuilderFactory.getInstance()
            .createBuilder(project)
            .console

    private fun createContent(tabName: String): Content =
        toolWindow.contentManager.factory.createContent(consoleView.component, tabName, true)
}