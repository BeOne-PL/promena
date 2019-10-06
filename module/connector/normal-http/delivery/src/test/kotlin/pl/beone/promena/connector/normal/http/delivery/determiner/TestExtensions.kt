package pl.beone.promena.connector.normal.http.delivery.determiner

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.util.CollectionUtils

internal val additionalHeaders = mapOf(
    ACCEPT to "application/pdf; charset=utf-8",
    LAST_MODIFIED to "Tue, 15 Nov 1994 12:45:26 GMT",
    TRANSFER_ENCODING to "chunked",
    USER_AGENT to "Mozilla/5.0 (X11; U; Linux i686; pl; rv:1.8.0.1) Gecko/20060124 Firefox/1.5.0.1"
)

internal fun Map<String, String>.toHttpHeaders(): HttpHeaders =
    HttpHeaders(CollectionUtils.toMultiValueMap(map { (key, value) -> key to listOf(value) }.toMap()))