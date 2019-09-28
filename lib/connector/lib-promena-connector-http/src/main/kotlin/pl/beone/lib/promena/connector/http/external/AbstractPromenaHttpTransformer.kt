package pl.beone.lib.promena.connector.http.external

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_OCTET_STREAM
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.netty.handler.codec.http.HttpResponseStatus.OK
import pl.beone.lib.promena.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
import pl.beone.lib.promena.connector.http.applicationmodel.exception.HttpException
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.netty.ByteBufFlux
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.util.function.Tuple2

abstract class AbstractPromenaHttpTransformer(
    private val serializationService: SerializationService,
    private val httpClient: HttpClient
) {

    fun <I> transform(data: I, httpAddress: String? = null): Mono<PerformedTransformationDescriptor> {
        val serializedInput = Mono.just(data)
            .map(serializationService::serialize)

        return httpClient
            .setContentTypeHeader()
            .post()
            .setUri(httpAddress)
            .send(ByteBufFlux.fromInbound(serializedInput))
            .responseSingle { response, bytes -> zipBytesWithResponse(bytes, response) }
            .map { byteArrayAndClientResponse -> handleTransformationResult(byteArrayAndClientResponse.t2, byteArrayAndClientResponse.t1) }
    }

    private fun HttpClient.setContentTypeHeader(): HttpClient =
        headers { it.set(CONTENT_TYPE, APPLICATION_OCTET_STREAM) }

    private fun HttpClient.RequestSender.setUri(httpAddress: String?): HttpClient.RequestSender =
        uri(if (httpAddress != null) "http://$httpAddress/transform" else "/transform")

    // defaultIfEmpty is necessary. In other case complete event is emitted if content is null
    private fun zipBytesWithResponse(byte: ByteBufMono, response: HttpClientResponse): Mono<Tuple2<ByteArray, HttpClientResponse>> =
        byte.asByteArray().defaultIfEmpty(ByteArray(0)).zipWith(response.toMono())

    private fun handleTransformationResult(clientResponse: HttpClientResponse, bytes: ByteArray): PerformedTransformationDescriptor =
        when (clientResponse.status()) {
            OK ->
                serializationService.deserialize(bytes, getClazz())
            INTERNAL_SERVER_ERROR ->
                throw serializationService.deserialize(bytes, clientResponse.responseHeaders().getSerializationClass())
            else ->
                throw HttpException(clientResponse.status(), bytes)
        }

    private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java

    @Suppress("UNCHECKED_CAST")
    private fun <T> HttpHeaders.getSerializationClass(): Class<T> =
        try {
            Class.forName(
                get(SERIALIZATION_CLASS)
                    ?: throw NoSuchElementException("Headers don't contain <$SERIALIZATION_CLASS> entry. An unknown error occurred")
            ) as Class<T>
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Class indicated in <$SERIALIZATION_CLASS> header isn't available", e)
        }
}