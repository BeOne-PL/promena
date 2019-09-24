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

class PromenaRunConfiguration(
    project: Project,
    factory: PromenaConfigurationFactory,
    name: String
) : RunConfigurationBase<PromenaSettingsEditorForm>(project, factory, name) {

    internal lateinit var host: String
    internal var port: Int = -1
    internal var repeat: Int = -1
    internal var concurrency: Int = -1

    override fun onNewConfigurationCreated() {
        host = "localhost"
        port = 8080
        repeat = 1
        concurrency = 1
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        PromenaSettingsEditor()

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? =
        null

    override fun canRunOn(target: ExecutionTarget): Boolean =
        false

    override fun checkConfiguration() {
        if (host.isBlank()) {
            throw RuntimeConfigurationError("Host isn't set")
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        XmlSerializer.deserializeInto(this, element)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        XmlSerializer.serializeInto(this, element)
    }
}
