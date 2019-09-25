package pl.beone.promena.intellij.plugin.extension

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import pl.beone.promena.intellij.plugin.configuration.PromenaConfigurationFactory
import pl.beone.promena.intellij.plugin.configuration.PromenaRunConfiguration
import pl.beone.promena.intellij.plugin.configuration.PromenaRunConfigurationType

internal fun RunManager.getSelectedPromenaRunnerAndConfigurationSettings(): RunnerAndConfigurationSettings? {
    val selectedConfiguration = selectedConfiguration
    return if (selectedConfiguration != null && selectedConfiguration.configuration is PromenaRunConfiguration) {
        selectedConfiguration
    } else {
        null
    }
}

internal fun RunManager.createPromenaRunnerAndConfigurationSettings(): RunnerAndConfigurationSettings {
    val uniqueName = suggestUniqueName(null, PromenaRunConfigurationType)
    return createConfiguration(uniqueName, PromenaConfigurationFactory(PromenaRunConfigurationType))
        .also(::addConfiguration)
        .also(::setTemporaryConfiguration)
}