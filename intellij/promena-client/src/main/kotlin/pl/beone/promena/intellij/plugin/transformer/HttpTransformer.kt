package pl.beone.promena.intellij.plugin.transformer

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.netty.ByteBufFlux
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.util.function.Tuple2

internal class HttpTransformer(private val serializationService: SerializationService) {

    companion object {
        private val httpClient = HttpClient.create()
    }

    fun transform(address: String, transformationDescriptor: TransformationDescriptor): Mono<PerformedTransformationDescriptor> {
        val serializedTransformationDescriptor = Mono.just(transformationDescriptor)
            .map(serializationService::serialize)

        return httpClient
            .setContentTypeHeader()
            .post()
            .uri("http://$address/transform")
            .send(ByteBufFlux.fromInbound(serializedTransformationDescriptor))
            .responseSingle { response, bytes -> zipBytesWithResponse(bytes, response) }
            .map { byteArrayAndClientResponse -> handleTransformationResult(byteArrayAndClientResponse.t2, byteArrayAndClientResponse.t1) }
    }

    private fun HttpClient.setContentTypeHeader(): HttpClient =
        headers { it.set(HttpHeaderNames.CONTENT_TYPE, MediaTypeConstants.APPLICATION_OCTET_STREAM.mimeType) }

    // defaultIfEmpty is necessary. In other case complete event is emitted if content is null
    private fun zipBytesWithResponse(byte: ByteBufMono, response: HttpClientResponse): Mono<Tuple2<ByteArray, HttpClientResponse>> =
        byte.asByteArray().defaultIfEmpty(ByteArray(0)).zipWith(response.toMono())

    private fun handleTransformationResult(clientResponse: HttpClientResponse, bytes: ByteArray): PerformedTransformationDescriptor =
        when (clientResponse.status()) {
            HttpResponseStatus.OK                    ->
                serializationService.deserialize(bytes, getClazz())
            HttpResponseStatus.INTERNAL_SERVER_ERROR ->
                throw serializationService.deserialize(bytes, clientResponse.responseHeaders().getSerializationClass())
            else                                     ->
                throw HttpException(clientResponse.status(), bytes)
        }

    private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java

    @Suppress("UNCHECKED_CAST")
    private fun <T> HttpHeaders.getSerializationClass(): Class<T> =
        try {
            Class.forName(
                get(PromenaHttpHeaders.SERIALIZATION_CLASS)
                    ?: throw NoSuchElementException("Headers don't contain <${PromenaHttpHeaders.SERIALIZATION_CLASS}> entry. An unknown error occurred on Promena.")
            ) as Class<T>
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Class indicated in <${PromenaHttpHeaders.SERIALIZATION_CLASS}> header isn't available", e)
        }
}
