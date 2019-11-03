package pl.beone.promena.lib.connector.http.external

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Headers.Companion.CONTENT_TYPE
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponse
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.lib.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
import pl.beone.promena.lib.connector.http.applicationmodel.exception.HttpException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK

class HttpPromenaTransformer(
    private val serializationService: SerializationService
) {

    suspend fun execute(transformationDescriptor: TransformationDescriptor, httpAddress: String): PerformedTransformationDescriptor =
        Fuel.post("http://$httpAddress/transform")
            .header(CONTENT_TYPE, APPLICATION_OCTET_STREAM.mimeType)
            .timeout(Int.MAX_VALUE)
            .timeoutRead(Int.MAX_VALUE)
            .body(serializationService.serialize(transformationDescriptor))
            .awaitByteArrayResponse()
            .let { handleTransformationResult(it.second, it.third) }

    private fun handleTransformationResult(response: Response, bytes: ByteArray): PerformedTransformationDescriptor =
        when (response.statusCode) {
            HTTP_OK ->
                serializationService.deserialize(bytes, getClazz())
            HTTP_INTERNAL_ERROR ->
                throw serializationService.deserialize(bytes, response.headers.getSerializationClass())
            else ->
                throw HttpException(response.statusCode, bytes)
        }

    private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java

    @Suppress("UNCHECKED_CAST")
    private fun <T> Headers.getSerializationClass(): Class<T> {
        if (!containsKey(SERIALIZATION_CLASS)) {
            throw NoSuchElementException("Headers don't contain <$SERIALIZATION_CLASS> entry. An unknown error occurred")
        }

        return try {
            Class.forName(this[SERIALIZATION_CLASS].first()) as Class<T>
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Class indicated in <$SERIALIZATION_CLASS> header isn't available", e)
        }
    }
}