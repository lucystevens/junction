package uk.co.lucystevens.junction.api.handlers

import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class HttpsRedirectHandler: HttpHandler {
    override fun handleRequest(exchange: HttpServerExchange) {
        // TODO implement
        Handlers.redirect("").handleRequest(exchange)
    }
}