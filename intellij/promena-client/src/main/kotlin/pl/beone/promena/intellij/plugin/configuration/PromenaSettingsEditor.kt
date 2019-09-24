package pl.beone.promena.intellij.plugin.configuration

import com.intellij.openapi.options.SettingsEditor
import javax.swing.JComponent

class PromenaSettingsEditor : SettingsEditor<PromenaRunConfiguration>() {

    private val form = PromenaSettingsEditorForm()

    override fun resetEditorFrom(configuration: PromenaRunConfiguration) {
        form.hostTextField.text = configuration.host
        form.portSpinner.value = configuration.port
        form.repeatSpinner.value = configuration.repeat
        form.concurrencySpinner.value = configuration.concurrency
    }

    override fun applyEditorTo(configuration: PromenaRunConfiguration) {
        configuration.host = form.hostTextField.text
        configuration.port = form.portSpinner.value.toString().toInt()
        configuration.repeat = form.repeatSpinner.value.toString().toInt()
        configuration.concurrency = form.concurrencySpinner.value.toString().toInt()
    }

    override fun createEditor(): JComponent = form.panel
}