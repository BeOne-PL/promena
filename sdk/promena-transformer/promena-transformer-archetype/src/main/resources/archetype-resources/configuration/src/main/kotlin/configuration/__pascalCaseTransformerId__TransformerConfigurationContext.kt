package ${package}.configuration

import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.time.Duration
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
            environment.getRequiredProperty("$PROPERTY_PREFIX.settings.example")
        )

    @Bean
    fun ${camelCaseTransformerId}TransformerDefaultParameters(environment: Environment): ${pascalCaseTransformerId}TransformerDefaultParameters =
        ${pascalCaseTransformerId}TransformerDefaultParameters(
            environment.getRequiredProperty("$PROPERTY_PREFIX.default.parameters.timeout").let { if (it.isNotBlank()) it.toDuration() else null },
            environment.getRequiredProperty("$PROPERTY_PREFIX.default.parameters.example2", String::class.java)
        )

    private fun String.toDuration(): Duration {
        val formatter = PeriodFormatterBuilder()
            .appendDays().appendSuffix("d ")
            .appendHours().appendSuffix("h ")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .appendMillis().appendSuffix("ms")
            .toFormatter()

        return Duration.ofMillis(formatter.parsePeriod(this).toStandardDuration().millis)
    }
}