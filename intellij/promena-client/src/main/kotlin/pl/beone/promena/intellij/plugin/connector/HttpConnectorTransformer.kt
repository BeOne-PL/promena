package pl.beone.promena.intellij.plugin.connector

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.net.HttpURLConnection
import java.net.URI

internal class HttpConnectorTransformer {

    private val httpClient = HttpClientBuilder.create().build()
    private val serializationService = KryoSerializationService()

    fun transform(address: String, transformationDescriptor: TransformationDescriptor): TransformedDataDescriptor =
        httpClient.execute(createRequest(address, transformationDescriptor)) {
            handleResponse(it)
        }.transformedDataDescriptor

    private fun createRequest(address: String, transformationDescriptor: TransformationDescriptor): HttpPost =
        HttpPost(address.createPromenaUri()).apply {
            entity = ByteArrayEntity(serializationService.serialize(transformationDescriptor))
        }

    private fun String.createPromenaUri(): URI =
        URI("http://${this}/transform?id=memory")

    private fun handleResponse(httpResponse: HttpResponse): PerformedTransformationDescriptor =
        if (httpResponse.statusLine.statusCode == HttpURLConnection.HTTP_OK) {
            serializationService.deserialize(httpResponse.readAllBytes(), PerformedTransformationDescriptor::class.java)
        } else {
            throw serializationService.deserialize(httpResponse.readAllBytes(), httpResponse.getSerializationClass())
        }

    private fun HttpResponse.readAllBytes(): ByteArray =
        entity.content.readBytes()

    @Suppress("UNCHECKED_CAST")
    private fun <T> HttpResponse.getSerializationClass(): Class<T> =
        try {
            Class.forName(
                getFirstHeader(PromenaHttpHeaders.SERIALIZATION_CLASS)?.value
                    ?: throw NoSuchElementException("Headers don't contain <${PromenaHttpHeaders.SERIALIZATION_CLASS}> entry. An unknown error occurred on Promena.")
            ) as Class<T>
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Class indicated in <${PromenaHttpHeaders.SERIALIZATION_CLASS}> header isn't available", e)
        }
}
