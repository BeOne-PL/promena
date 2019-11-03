package pl.beone.promena.lib.connector.http.applicationmodel.exception

class HttpException(
    val responseStatus: Int,
    val bytes: ByteArray
) : RuntimeException("Couldn't make request: $responseStatus - ${String(bytes)}")