package uk.co.lucystevens.junction.api.handlers.api

import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import io.undertow.util.Methods

data class ApiRoute(
    val method: HttpString,
    val path: String
)

val HttpServerExchange.route get() = ApiRoute(requestMethod, requestPath)

fun get(path: String) = ApiRoute(Methods.GET, path)
fun post(path: String) = ApiRoute(Methods.POST, path)
fun put(path: String) = ApiRoute(Methods.PUT, path)
fun delete(path: String) = ApiRoute(Methods.DELETE, path)