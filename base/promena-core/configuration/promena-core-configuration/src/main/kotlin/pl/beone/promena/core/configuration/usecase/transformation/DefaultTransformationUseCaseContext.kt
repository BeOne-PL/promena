package pl.beone.promena.core.configuration.usecase.transformation

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.CommunicationValidator
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.core.usecase.transformation.DefaultTransformationUseCase

@Configuration
class DefaultTransformationUseCaseContext {

    @Bean
    @ConditionalOnMissingBean(TransformationUseCase::class)
    fun defaultTransformationUseCase(dataDescriptorSerializationService: DescriptorSerializationService,
                                     communicationValidator: CommunicationValidator,
                                     incomingCommunicationConverter: IncomingCommunicationConverter,
                                     transformerService: TransformerService,
                                     outgoingCommunicationConverter: OutgoingCommunicationConverter) =
            DefaultTransformationUseCase(dataDescriptorSerializationService,
                                                                                      communicationValidator,
                                                                                      incomingCommunicationConverter,
                                                                                      transformerService,
                                                                                      outgoingCommunicationConverter)
}