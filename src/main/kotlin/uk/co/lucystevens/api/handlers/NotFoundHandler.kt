package uk.co.lucystevens.api.handlers

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class NotFoundHandler: HttpHandler {
    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.statusCode = 404
        exchange.responseSender.send("Not found")
    }
}