package uk.co.lucystevens.junction.api.handlers.api

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Methods
import io.undertow.util.StatusCodes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.handlers.NotFoundHandler
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.RouteService
import uk.co.lucystevens.junction.utils.asHandler
import uk.co.lucystevens.junction.utils.getHeader
import uk.co.lucystevens.junction.utils.ok

abstract class ApiHandler(
    private val config: Config
    ) : HttpHandler {

    private val tokenHeader = "token"

    abstract val path: String
    abstract val routes: Map<ApiRoute, HttpHandler>

    override fun handleRequest(exchange: HttpServerExchange) {
        val token = exchange.getHeader(tokenHeader)
        if(token.firstOrNull() != config.getAdminToken()){
            exchange.statusCode = StatusCodes.UNAUTHORIZED
            return
        }

        exchange.dispatch(
           routes[exchange.route] ?: NotFoundHandler()
        )
    }
}