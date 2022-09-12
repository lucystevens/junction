package uk.co.lucystevens.junction.api.handlers.api

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.Methods
import io.undertow.util.StatusCodes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.lucystevens.junction.api.dto.Route
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.RouteService
import uk.co.lucystevens.junction.utils.asHandler
import uk.co.lucystevens.junction.utils.getHeader
import uk.co.lucystevens.junction.utils.ok

class ApiHandler(
    private val routeService: RouteService,
    private val json: Json,
    private val config: Config,
    private val defaultHandlers: DefaultHandlers
    ) : HttpHandler {

    private val tokenHeader = "token"
    private val path = "/api/routes"

    override fun handleRequest(exchange: HttpServerExchange) {
        val token = exchange.getHeader(tokenHeader)
        if(token.firstOrNull() != config.getAdminToken()){
            exchange.statusCode = StatusCodes.UNAUTHORIZED
            return
        }

        exchange.dispatch(
           if (exchange.requestPath == path){
                when(exchange.requestMethod){
                    Methods.GET -> asHandler { getRoutes(it) }
                    Methods.POST -> asHandler { putRoute(it) }
                    Methods.PUT -> asHandler { putRoute(it) }
                    Methods.DELETE -> asHandler { removeRoute(it) }
                    else -> defaultHandlers.notFoundHandler
                }
            }
            else defaultHandlers.notFoundHandler
        )
    }

    fun getRoutes(exchange: HttpServerExchange) {
        val routes = routeService.getRoutes()
            .map { RouteDto(it.first, it.second) }
        exchange.ok(json.encodeToString(routes))
    }

    fun putRoute(exchange: HttpServerExchange) {
        exchange.requestReceiver.receiveFullString { ex, body ->
            val dto = json.decodeFromString<RouteDto>(body)
            println(dto)
            // validate targets
            dto.options.targets.map { it.toURI() }
            // TODO if SSL enabled, validate cert
            routeService.putRoute(dto.route, dto.options)
            ex.statusCode = StatusCodes.NO_CONTENT
        }
    }

    fun removeRoute(exchange: HttpServerExchange) {
        exchange.requestReceiver.receiveFullString { ex, body ->
            val dto = json.decodeFromString<Route>(body)
            routeService.removeRoute(dto)
            ex.statusCode = StatusCodes.NO_CONTENT
        }
    }
}