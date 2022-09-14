package uk.co.lucystevens.junction.utils

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.StatusCodes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun HttpServerExchange.getHeader(header: String): List<String> =
    if(!requestHeaders.contains(header)) listOf()
    else requestHeaders[header]

fun asHandler(handler: (exchange: HttpServerExchange) -> Unit) =
    FunctionHandler(handler)

class FunctionHandler(
    private val handler: (exchange: HttpServerExchange) -> Unit
) : HttpHandler{
    override fun handleRequest(exchange: HttpServerExchange) {
        handler(exchange)
    }
}

fun dispatch(handler: (exchange: HttpServerExchange) -> HttpHandler) =
    DispatchHandler(handler)

class DispatchHandler(
    private val handler: (exchange: HttpServerExchange) -> HttpHandler
) : HttpHandler{
    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.dispatch(handler(exchange))
    }
}

inline fun <reified T> HttpServerExchange.useBody(json: Json, crossinline callback: (T) -> Unit) =
    requestReceiver.receiveFullString { _, body ->
        callback(json.decodeFromString(body))
    }

fun HttpServerExchange.ok(content: String, contentType: String = "application/json"){
    statusCode = StatusCodes.OK
    responseHeaders.add(Headers.CONTENT_TYPE, contentType)
    responseSender.send(content)
}

fun HttpServerExchange.noContent() {
    statusCode = StatusCodes.NO_CONTENT
}