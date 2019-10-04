package pl.beone.promena.lib.connector.http.applicationmodel.exception

import io.netty.handler.codec.http.HttpResponseStatus

class HttpException(
    val responseStatus: HttpResponseStatus,
    val bytes: ByteArray
) : RuntimeException("Couldn't make request: $responseStatus - ${String(bytes)}")