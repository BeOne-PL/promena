package ${package}.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationWritableDataCreator
import ${package}.${pascalCaseTransformerId}Transformer
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

@Configuration
class ${pascalCaseTransformerId}TransformerContext {

    @Bean
    fun ${camelCaseTransformerId}Transformer(
        settings: ${pascalCaseTransformerId}TransformerSettings,
        defaultParameters: ${pascalCaseTransformerId}TransformerDefaultParameters,
        @Qualifier("internalCommunicationParameters") communicationParameters: CommunicationParameters,
        @Qualifier("internalCommunicationWritableDataCreator") communicationWritableDataCreator: CommunicationWritableDataCreator
    ) =
        ${pascalCaseTransformerId}Transformer(
            settings,
            defaultParameters,
            communicationParameters,
            communicationWritableDataCreator
        )
}