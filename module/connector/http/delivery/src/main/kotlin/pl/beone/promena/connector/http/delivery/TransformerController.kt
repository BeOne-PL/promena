package pl.beone.promena.connector.http.delivery

import mu.KotlinLogging
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@RestController
class TransformerController(
    private val serializationService: SerializationService,
    private val transformationUseCase: TransformationUseCase
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostMapping("/transform", consumes = [APPLICATION_OCTET_STREAM_VALUE])
    fun handle(@RequestBody body: Mono<ByteArray>): Mono<ResponseEntity<ByteArray>> =
        body
            .map(::deserializeTransformationDescriptor)
            .map { (transformation, dataDescriptor, communicationParameters) ->
                performedTransformationDescriptor(transformationUseCase.transform(transformation, dataDescriptor, communicationParameters))
            }
            .map(serializationService::serialize)
            .map(::createResponse)
            .onErrorResume({ it !is ResponseStatusException }) {
                logUnknownException(it)
                createInternalServerErrorResponse(it).let { Mono.just(it) }
            }

    private fun deserializeTransformationDescriptor(byteArray: ByteArray): TransformationDescriptor =
        serializationService.deserialize(byteArray, getClazz())

    private fun createResponse(bytes: ByteArray): ResponseEntity<ByteArray> =
        ResponseEntity.ok().body(bytes)

    private fun logUnknownException(exception: Throwable) {
        if (exception !is TransformationException) {
            logger.error(exception) { "An error occurred before starting given transformation" }
        }
    }

    private fun createInternalServerErrorResponse(exception: Throwable): ResponseEntity<ByteArray> =
        ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .header(SERIALIZATION_CLASS, exception.javaClass.name)
            .body(serializationService.serialize(exception))

}