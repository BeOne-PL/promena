package ${package}.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import ${package}.configuration.extension.getNotBlankProperty
import ${package}.configuration.extension.getRequiredNotBlankProperty
import ${package}.configuration.extension.toDuration
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

@Configuration
class ${pascalCaseTransformerId}TransformerConfigurationContext {

    companion object {
        private const val PROPERTY_PREFIX = "transformer.${package}"
    }

    @Bean
    fun ${camelCaseTransformerId}TransformerSettings(environment: Environment): ${pascalCaseTransformerId}TransformerSettings =
        ${pascalCaseTransformerId}TransformerSettings(
            environment.getRequiredNotBlankProperty("$PROPERTY_PREFIX.settings.example")
        )

    @Bean
    fun ${camelCaseTransformerId}TransformerDefaultParameters(environment: Environment): ${pascalCaseTransformerId}TransformerDefaultParameters =
        ${pascalCaseTransformerId}TransformerDefaultParameters(
            environment.getRequiredNotBlankProperty("$PROPERTY_PREFIX.default.parameters.example2"),
            environment.getNotBlankProperty("$PROPERTY_PREFIX.default.parameters.timeout")?.toDuration()
        )
}