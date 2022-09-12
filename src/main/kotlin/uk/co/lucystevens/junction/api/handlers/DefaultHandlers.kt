package uk.co.lucystevens.junction.api.handlers

import io.undertow.server.HttpHandler

data class DefaultHandlers(
    val httpsRedirectHandler: HttpHandler,
    val notFoundHandler: HttpHandler
)