package pl.beone.promena.core.configuration.usecase.transformation

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.core.usecase.transformation.DefaultTransformationUseCase
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class DefaultTransformationUseCaseContext {

    @Bean
    @ConditionalOnMissingBean(TransformationUseCase::class)
    fun defaultTransformationUseCase(externalCommunicationManager: ExternalCommunicationManager,
                                     internalCommunicationParameters: CommunicationParameters,
                                     transformerService: TransformerService) =
            DefaultTransformationUseCase(externalCommunicationManager,
                                         internalCommunicationParameters,
                                         transformerService)
}