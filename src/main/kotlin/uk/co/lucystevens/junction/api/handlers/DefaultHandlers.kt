package uk.co.lucystevens.junction.api.handlers

import io.undertow.server.HttpHandler

data class DefaultHandlers(
    val httpHandler: HttpHandler,
    val httpsHandler: HttpHandler,
    val apiHandler: HttpHandler
)
