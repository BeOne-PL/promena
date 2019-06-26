package pl.beone.promena.connector.http.delivery.http

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import reactor.core.publisher.Mono

class TransformerHandler(private val serializationService: SerializationService,
                         private val descriptorSerializationService: DescriptorSerializationService,
                         private val transformationUseCase: TransformationUseCase) {

    companion object {
        private const val HEADER_SERIALIZATION_CLASS = "serialization-class"
    }

    private val communicationParametersConverter = CommunicationParametersConverter()

    fun transform(serverRequest: ServerRequest): Mono<ServerResponse> =
            transform(serverRequest,
                      serverRequest.pathVariable("transformerId"),
                      communicationParametersConverter.convert(serverRequest.queryParams()))

    private fun transform(serverRequest: ServerRequest, transformerId: String, parameters: CommunicationParameters): Mono<ServerResponse> =
            serverRequest.bodyToMono(ByteArray::class.java)
                    .map { descriptorSerializationService.deserialize(it) }
                    .map { transformationUseCase.transform(transformerId, it, parameters) }
                    .map { descriptorSerializationService.serialize(it) }
                    .flatMap { createResponse(it) }
                    .onErrorResume { createExceptionResponse(it) }

    private fun createResponse(bytes: ByteArray): Mono<ServerResponse> =
            ServerResponse.ok().body(Mono.just(bytes), ByteArray::class.java)

    private fun createExceptionResponse(exception: Throwable): Mono<ServerResponse> =
            ServerResponse.status(INTERNAL_SERVER_ERROR)
                    .header(HEADER_SERIALIZATION_CLASS, exception.javaClass.name)
                    .body(Mono.just(exception).map { serializationService.serialize(it) }, ByteArray::class.java)
}