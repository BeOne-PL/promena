package pl.beone.promena.intellij.plugin.configuration

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element
import pl.beone.promena.intellij.plugin.configuration.PromenaRunConfigurationValidator.Host
import pl.beone.promena.intellij.plugin.configuration.PromenaRunConfigurationValidator.Port

class PromenaRunConfiguration(
    project: Project,
    factory: PromenaConfigurationFactory,
    name: String
) : RunConfigurationBase<PromenaSettingsEditor>(project, factory, name) {

    internal var host: String = "localhost"
    internal var port: Int = 8080

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        PromenaSettingsEditor()

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? =
        null

    override fun canRunOn(target: ExecutionTarget): Boolean =
        false

    override fun checkConfiguration() {
        if (!Host.validate(host)) {
            throw RuntimeConfigurationError("Host must be set")
        }

        if (!Port.validate(port)) {
            throw RuntimeConfigurationError("Port must be in range ${Port.RANGE.toDescription()}")
        }
    }

    private fun IntRange.toDescription(): String =
        "[$start, $last]"

    override fun readExternal(element: Element) {
        super.readExternal(element)
        XmlSerializer.deserializeInto(this, element)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        XmlSerializer.serializeInto(this, element)
    }
}
