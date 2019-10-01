package ${package}.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import ${package}.${pascalCaseTransformerId}Transformer
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters

@Configuration
class ${pascalCaseTransformerId}TransformerContext {

    @Bean
    fun ${camelCaseTransformerId}Transformer(
        environment: Environment,
        internalCommunicationParameters: CommunicationParameters
    ) =
        ${pascalCaseTransformerId}Transformer(
            createDefaultParameters(environment),
            internalCommunicationParameters
        )

    private fun createDefaultParameters(environment: Environment): ${pascalCaseTransformerId}TransformerDefaultParameters =
        ${pascalCaseTransformerId}TransformerDefaultParameters(
            environment.getRequiredProperty("transformer.${package}.default.parameters.example2")
        )
}