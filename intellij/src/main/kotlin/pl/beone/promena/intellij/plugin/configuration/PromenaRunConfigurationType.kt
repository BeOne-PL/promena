package pl.beone.promena.intellij.plugin.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object PromenaRunConfigurationType : ConfigurationType {

    private val icon = IconLoader.getIcon("/icon/promena.svg", PromenaRunConfigurationType::class.java)

    override fun getDisplayName(): String =
        "Promena"

    override fun getConfigurationTypeDescription(): String? =
        null

    override fun getIcon(): Icon =
        icon

    override fun getId(): String =
        "PROMENA_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(PromenaConfigurationFactory(this))
}
