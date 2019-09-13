package pl.beone.promena.connector.http.delivery.http

import org.springframework.util.MultiValueMap
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders

internal class HeadersToSentBackDeterminer {

    fun determine(headers: MultiValueMap<String, String>): Map<String, List<String>> =
        headers.filter { it.key.startsWith(PromenaHttpHeaders.SEND_BACK_PREFIX) }
}
