package pl.beone.promena.intellij.plugin.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import javax.swing.Icon

object PromenaRunConfigurationType : ConfigurationType {

    override fun getDisplayName(): String =
        "Promena"

    override fun getConfigurationTypeDescription(): String? =
        null

    override fun getIcon(): Icon? =
        null

    override fun getId(): String =
        "PROMENA_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(PromenaConfigurationFactory(this))
}
