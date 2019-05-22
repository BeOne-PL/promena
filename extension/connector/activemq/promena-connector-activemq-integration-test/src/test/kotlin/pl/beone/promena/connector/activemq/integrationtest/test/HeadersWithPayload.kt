package pl.beone.promena.connector.activemq.integrationtest.test

data class HeadersWithPayload<T>(val headers: Map<String, Any>,
                                 val payload: T)