package pl.beone.promena.connector.http.delivery.http

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import reactor.core.publisher.Mono

class TransformerHandler(
    private val serializationService: SerializationService,
    private val transformationUseCase: TransformationUseCase
) {

    companion object {
        private val communicationParametersConverter = CommunicationParametersConverter()
    }

    fun transform(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.bodyToMono(ByteArray::class.java)
            .map(::deserializeTransformationDescriptor)
            .map { (transformation, dataDescriptor) ->
                PerformedTransformationDescriptor.of(
                    transformation,
                    transformationUseCase.transform(transformation, dataDescriptor, communicationParametersConverter.convert(serverRequest.queryParams()))
                )
            }
            .map(serializationService::serialize)
            .flatMap(::createResponse)
            // CommunicationParametersConverter -> no <id> communication parameter
            .onErrorMap(NoSuchElementException::class.java) { ResponseStatusException(HttpStatus.BAD_REQUEST, it.message!!) }
            .onErrorResume({ it !is ResponseStatusException }, ::createExceptionResponse)

    private fun deserializeTransformationDescriptor(byteArray: ByteArray): TransformationDescriptor =
        serializationService.deserialize(
            byteArray,
            getClazz()
        )

    private fun createResponse(bytes: ByteArray): Mono<ServerResponse> =
        ServerResponse.ok().body(Mono.just(bytes), ByteArray::class.java)

    private fun createExceptionResponse(exception: Throwable): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(PromenaHttpHeaders.SERIALIZATION_CLASS, exception.javaClass.name)
            .body(Mono.just(exception).map(serializationService::serialize), ByteArray::class.java)

}