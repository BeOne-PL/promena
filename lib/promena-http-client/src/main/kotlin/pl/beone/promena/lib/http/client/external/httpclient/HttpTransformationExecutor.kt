package pl.beone.promena.lib.http.client.external.httpclient

import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.lib.http.client.applicationmodel.exception.TransformationException
import pl.beone.promena.lib.http.client.contract.request.TransformationExecutor
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class HttpTransformationExecutor(private val protocol: String,
                                 private val host: String,
                                 private val port: Int,
                                 connectionTimeout: Int,
                                 private val maxConnections: Int) : TransformationExecutor {

    private val httpClient: HttpClient

    init {
        val config = RequestConfig.custom()
                .setConnectTimeout(0)
                .setConnectionRequestTimeout(connectionTimeout)
                .setSocketTimeout(0)
                .build()

        val poolingHttpClientConnectionManager = PoolingHttpClientConnectionManager().apply {
            maxTotal = maxConnections
            defaultMaxPerRoute = maxConnections
        }

        httpClient = HttpClients.custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(config)
                .build()
    }

    override fun execute(transformerId: String, bytes: ByteArray, parameters: CommunicationParameters, timeout: Long): ByteArray {
        val entity = EntityBuilder.create()
                .setBinary(bytes)
                .build()

        val uri = createUri(transformerId, parameters)
        val httpPost = HttpPost(uri).apply {
            this.entity = entity
        }

        return try {
            Executors.newSingleThreadExecutor().submit(Callable<ByteArray> { httpPost.makeRequest() })
                    .get(timeout, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            httpPost.abort()
            throw e
        }
    }

    private fun createUri(transformerId: String, parameters: CommunicationParameters): URI {
        val uriBuilder = URIBuilder()
                .setScheme(protocol)
                .setHost(host)
                .setPort(port)
                .setPath("/transform/$transformerId")

        parameters.getAll().forEach { uriBuilder.addParameter(it.key, it.value.toString()) }

        return uriBuilder.build()
    }

    private fun HttpRequestBase.makeRequest(): ByteArray {
        val response = httpClient.execute(this)
        val entity = response.entity

        verifyResponseStatus(response.statusLine.statusCode, entity)

        return EntityUtils.toByteArray(entity)
    }

    private fun verifyResponseStatus(statusCode: Int, entity: HttpEntity) {
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw TransformationException("Couldn't transform. Error occurred on the server side. Response (status code: <$statusCode>): \n" +
                                                  "<${EntityUtils.toString(entity, "UTF-8")}>")
        }
    }
}