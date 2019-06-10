package pl.beone.promena.connector.http.delivery.http

import org.springframework.web.reactive.function.server.RouterFunctions

fun route(transformerHandler: TransformerHandler) =
        RouterFunctions.route()
                .POST("/transform/{transformerId}") { transformerHandler.transform(it) }
                .build()