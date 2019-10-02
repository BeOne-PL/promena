package pl.beone.promena.connector.http.delivery.http

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import reactor.core.publisher.Mono

class TransformerHandler(
    private val serializationService: SerializationService,
    private val transformationUseCase: TransformationUseCase
) : HandlerFunction<ServerResponse> {

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.bodyToMono(ByteArray::class.java)
            .map(::deserializeTransformationDescriptor)
            .map { (transformation, dataDescriptor, communicationParameters) ->
                performedTransformationDescriptor(
                    transformation,
                    transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
                )
            }
            .map(serializationService::serialize)
            .flatMap(::createResponse)
            .onErrorResume({ it !is ResponseStatusException }, ::createInternalServerErrorResponse)

    private fun deserializeTransformationDescriptor(byteArray: ByteArray): TransformationDescriptor =
        serializationService.deserialize(byteArray, getClazz())

    private fun createResponse(bytes: ByteArray): Mono<ServerResponse> =
        ServerResponse.ok().body(Mono.just(bytes), ByteArray::class.java)

    private fun createInternalServerErrorResponse(exception: Throwable): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(SERIALIZATION_CLASS, exception.javaClass.name)
            .body(Mono.just(exception).map(serializationService::serialize), ByteArray::class.java)

}