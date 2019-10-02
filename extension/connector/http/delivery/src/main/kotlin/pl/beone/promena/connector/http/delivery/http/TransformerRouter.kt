package pl.beone.promena.connector.http.delivery.http

import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

fun route(handler: TransformerHandler): RouterFunction<ServerResponse> =
    RouterFunctions.route()
        .POST("/transform", handler)
        .build()