package pl.beone.promena.connector.messagebroker.integrationtest.test

data class HeadersWithPayload<T>(val headers: Map<String, Any>,
                                 val payload: T)