package pl.beone.promena.core.configuration.usecase.transformation

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.usecase.transformation.DefaultTransformationUseCase

@Configuration
class DefaultTransformationUseCaseContext {

    @Bean
    @ConditionalOnMissingBean(TransformationUseCase::class)
    fun defaultTransformationUseCase(
        externalCommunicationManager: ExternalCommunicationManager,
        transformationService: TransformationService
    ) =
        DefaultTransformationUseCase(
            externalCommunicationManager,
            transformationService
        )
}