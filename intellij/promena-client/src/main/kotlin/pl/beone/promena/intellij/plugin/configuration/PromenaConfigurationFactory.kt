package pl.beone.promena.intellij.plugin.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class PromenaConfigurationFactory(
    type: ConfigurationType
) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        PromenaRunConfiguration(project, this, "Promena")
}
