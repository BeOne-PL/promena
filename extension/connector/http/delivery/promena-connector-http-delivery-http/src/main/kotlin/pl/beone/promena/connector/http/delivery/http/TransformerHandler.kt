package pl.beone.promena.connector.http.delivery.http

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import reactor.core.publisher.Mono

class TransformerHandler(private val transformationUseCase: TransformationUseCase) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerHandler::class.java)
    }

    private val communicationParametersConverter = CommunicationParametersConverter()

    fun transform(serverRequest: ServerRequest): Mono<ServerResponse> {
        val transformerId = serverRequest.pathVariable("transformerId")
        val parameters = serverRequest.queryParams()

        val transformation = serverRequest.bodyToMono(ByteArray::class.java)
                .map { transformationUseCase.transform(transformerId, it, communicationParametersConverter.convert(parameters)) }

        return ServerResponse.ok()
                .body(transformation.onErrorResume { Mono.error(createException(it)) }, ByteArray::class.java)
    }

    private fun createException(exception: Throwable): ResponseStatusException =
            ResponseStatusException(determineHttpStatus(exception), exception.message, exception)

    private fun determineHttpStatus(exception: Throwable): HttpStatus =
            when (exception) {
                is TransformerNotFoundException -> BAD_REQUEST
                is TransformerTimeoutException  -> REQUEST_TIMEOUT
                else                            -> INTERNAL_SERVER_ERROR
            }
}