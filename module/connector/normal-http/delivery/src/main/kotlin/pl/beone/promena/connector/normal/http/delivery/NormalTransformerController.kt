package pl.beone.promena.connector.normal.http.delivery

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.connector.normal.http.delivery.determiner.MediaTypeDeterminer
import pl.beone.promena.connector.normal.http.delivery.determiner.TransformationDeterminer
import pl.beone.promena.connector.normal.http.delivery.extension.toHttpString
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class NormalTransformerController(
    private val transformationUseCase: TransformationUseCase
) {

    /**
     * The flow:
     * 1. Receives `multipart/form-data` `POST` request on `/normal/transform`
     * 2. Converts `transformation{NUMBER}-transformerId-name`, `transformation{NUMBER}-transformerId-subName`,
     *    `transformation{NUMBER}-mediaType-mimeType`, `transformation{NUMBER}-mediaType-charset` headers
     *    into [Transformation] ([TransformationDeterminer.determine]) in the order determined by `{NUMBER}`
     * 3. Converts all `Form-Data` into [DataDescriptor]
     * 4. Performs a transformation
     * 5. Returns data as the body with [HttpStatus.OK] response status
     * 6. In case of an error, it converts an exception using [mapException]:
     *
     * It doesn't support:
     * - Passing [Parameters][pl.beone.promena.transformer.contract.model.Parameters] of [Transformation]
     * - Passing [Metadata] of [DataDescriptor]
     * - Passing external [CommunicationParameters][pl.beone.promena.transformer.contract.communication.CommunicationParameters]
     * - Returning more than 1 [TransformedDataDescriptor]
     */
    @PostMapping("/normal/transform", consumes = [org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE])
    fun transform(@RequestHeader headers: HttpHeaders, @RequestBody parts: Flux<Part>): Mono<ResponseEntity<ByteArray>> =
        parts
            .flatMap { reduceToMono(it.content()).zipWith(MediaTypeDeterminer.determine(it.name(), it.headers()).let { Mono.just(it) }) }
            .map { singleDataDescriptor(it.t1.readBytes().toMemoryData(), it.t2, emptyMetadata()) }
            .reduce(emptyList<DataDescriptor.Single>()) { accumulator, element -> accumulator + element }
            .map(::dataDescriptor)
            .map { dataDescriptor ->
                val transformation = TransformationDeterminer.determine(headers)
                transformationUseCase.transform(transformation, dataDescriptor, memoryCommunicationParameters()) to
                        determineTransformationTargetMediaType(transformation)
            }
            .doOnNext { (transformedDataDescriptor) -> validate(transformedDataDescriptor) }
            .map { (transformedDataDescriptor, targetMediaType) -> createResponse(transformedDataDescriptor, targetMediaType) }
            .onErrorMap(::mapException)

    private fun determineTransformationTargetMediaType(transformation: Transformation): MediaType =
        transformation.transformers.last().targetMediaType

    private fun reduceToMono(dataBuffer: Flux<DataBuffer>): Mono<DataBuffer> =
        dataBuffer.reduce { accumulator: DataBuffer, element: DataBuffer -> accumulator.write(element) }

    private fun DataBuffer.readBytes(): ByteArray =
        asInputStream().readAllBytes()

    private fun validate(transformedDataDescriptor: TransformedDataDescriptor) {
        check(transformedDataDescriptor.descriptors.size == 1) { "There is more than one transformed data: <${transformedDataDescriptor.descriptors.size}>" }
    }

    private fun getFirstDataInputBytes(transformedDataDescriptor: TransformedDataDescriptor): ByteArray =
        transformedDataDescriptor.descriptors.first().data.getBytes()

    private fun createResponse(transformedDataDescriptor: TransformedDataDescriptor, targetMediaType: MediaType): ResponseEntity<ByteArray> =
        ResponseEntity.ok()
            .header(CONTENT_TYPE, targetMediaType.toHttpString())
            .body(getFirstDataInputBytes(transformedDataDescriptor))

    private fun mapException(e: Throwable): Throwable =
        ResponseStatusException(
            when {
                e is IllegalStateException -> BAD_REQUEST
                e is TransformationException && e.causeClass == TransformationNotSupportedException::class.java.canonicalName -> BAD_REQUEST
                e is TransformationException && e.causeClass == TransformerNotFoundException::class.java.canonicalName -> BAD_REQUEST
                e is TransformationException && e.causeClass == TransformerTimeoutException::class.java.canonicalName -> REQUEST_TIMEOUT
                else -> INTERNAL_SERVER_ERROR
            },
            e.message
        )
}