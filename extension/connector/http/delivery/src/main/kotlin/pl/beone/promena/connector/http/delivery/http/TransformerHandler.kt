package pl.beone.promena.connector.http.delivery.http

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import reactor.core.publisher.Mono

class TransformerHandler(
    private val serializationService: SerializationService,
    private val transformationUseCase: TransformationUseCase
) {

    companion object {
        private val headersToSentBackDeterminer = HeadersToSentBackDeterminer()
    }

    fun transform(serverRequest: ServerRequest): Mono<ServerResponse> =
        serverRequest.bodyToMono(ByteArray::class.java)
            .map(::deserializeTransformationDescriptor)
            .map { (transformation, dataDescriptor, communicationParameters) ->
                performedTransformationDescriptor(
                    transformation,
                    transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
                )
            }
            .map(serializationService::serialize)
            .flatMap { createResponse(it, headersToSentBackDeterminer.determine(serverRequest.headers().asHttpHeaders())) }
            .onErrorResume({ it !is ResponseStatusException }, ::createInternalServerErrorResponse)

    private fun deserializeTransformationDescriptor(byteArray: ByteArray): TransformationDescriptor =
        serializationService.deserialize(byteArray, getClazz())

    private fun createResponse(bytes: ByteArray, headers: Map<String, List<String>>): Mono<ServerResponse> =
        ServerResponse.ok()
            .addAll(headers)
            .body(Mono.just(bytes), ByteArray::class.java)

    private fun createInternalServerErrorResponse(exception: Throwable): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(PromenaHttpHeaders.SERIALIZATION_CLASS, exception.javaClass.name)
            .body(Mono.just(exception).map(serializationService::serialize), ByteArray::class.java)

    private fun ServerResponse.BodyBuilder.addAll(headers: Map<String, List<String>>): ServerResponse.BodyBuilder =
        headers { consumer -> headers.forEach { (key, value) -> consumer.addAll(key, value) } }
}