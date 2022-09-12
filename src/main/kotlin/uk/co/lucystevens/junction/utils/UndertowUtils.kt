package uk.co.lucystevens.junction.utils

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.StatusCodes
import kotlinx.serialization.encodeToString

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

fun HttpServerExchange.ok(content: String, contentType: String = "application/json"){
    statusCode = StatusCodes.OK
    responseHeaders.add(Headers.CONTENT_TYPE, contentType)
    responseSender.send(content)
}