package pl.skotar.intellij.plugin.alfrescojvmconsole.dialog

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import pl.beone.promena.intellij.plugin.extension.toFullString
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.*

class ErrorDialog(
    project: Project,
    exception: Exception
) : DialogWrapper(project, false) {

    internal lateinit var panel: JPanel
    internal lateinit var errorScrollPane: JScrollPane

    internal lateinit var closeButton: JButton
    internal lateinit var restartButton: JButton
    internal lateinit var buttonPanel: JPanel

    private lateinit var errorTextPane: JTextPane

    init {
        init()

        setUpCloseButton()
        setUpRestartButton()
        printException(exception)
        setDialogSize()
    }

    private fun createUIComponents() {
        errorTextPane = JTextPane()
        errorScrollPane = JBScrollPane(errorTextPane)
    }

    private fun setUpCloseButton() {
        closeButton.addActionListener {
            close(0)
        }
    }

    private fun setUpRestartButton() {
        restartButton.addActionListener {
            val application = ApplicationManager.getApplication()
            TransactionGuard.submitTransaction(application, Runnable { (application as ApplicationImpl).exit(true, false, true) })
        }
    }

    private fun printException(exception: Exception) {
        errorTextPane.text = exception.toFullString()
    }

    private fun setDialogSize() {
        val screenDimension = Toolkit.getDefaultToolkit().screenSize
        panel.preferredSize = Dimension(minOf(screenDimension.width, 600), minOf(screenDimension.height, 600))
    }

    override fun createCenterPanel(): JComponent =
        panel

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel =
        buttonPanel
}