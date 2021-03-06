package pl.beone.promena.intellij.plugin.configuration

import com.intellij.openapi.options.SettingsEditor
import pl.beone.promena.intellij.plugin.configuration.PromenaRunConfigurationValidator.Port
import javax.swing.*

class PromenaSettingsEditor : SettingsEditor<PromenaRunConfiguration>() {

    internal lateinit var panel: JPanel
    internal lateinit var hostTextField: JTextField
    internal lateinit var portSpinner: JSpinner

    override fun createEditor(): JComponent {
        portSpinner.setEditorWithoutGroupingAndModel(Port.RANGE.first, Port.RANGE.last)

        return panel
    }

    private fun JSpinner.setEditorWithoutGroupingAndModel(minimum: Int, maximum: Int) {
        model = SpinnerNumberModel(minimum, minimum, maximum, 1)
        editor = JSpinner.NumberEditor(this, "#")
    }

    override fun resetEditorFrom(configuration: PromenaRunConfiguration) {
        hostTextField.text = configuration.host
        portSpinner.value = configuration.port
    }

    override fun applyEditorTo(configuration: PromenaRunConfiguration) {
        configuration.host = hostTextField.text
        configuration.port = portSpinner.value.toString().toInt()
    }
}