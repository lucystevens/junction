package uk.co.lucystevens.junction.api.handlers.api

import io.undertow.server.HttpServerExchange
import io.undertow.util.BadRequestException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.RouteService
import uk.co.lucystevens.junction.utils.asHandler
import uk.co.lucystevens.junction.utils.noContent
import uk.co.lucystevens.junction.utils.ok
import uk.co.lucystevens.junction.utils.useBody

class RoutesApiHandler(
    private val routeService: RouteService,
    private val json: Json,
    config: Config,
    ) : ApiHandler(config) {

    override val path = "/api/routes"

    override val routes = mapOf(
        get(path) to asHandler { listRoutes(it) },
        post(path) to apiRoute { putRoute(it) },
        put(path) to apiRoute { putRoute(it) },
        delete(path) to apiRoute { removeRoute(it) }
    )

    fun listRoutes(exchange: HttpServerExchange) {
        val routes = routeService.getRoutes()
            .map { RouteDto(it.first, it.second) }
        exchange.ok(json.encodeToString(routes))
    }

    // TODO also accept list of routes
    fun putRoute(exchange: HttpServerExchange) {
        exchange.useBody<RouteDto>(json){ dto ->
            // validate targets
            if(dto.targets.isEmpty()){
                throw BadRequestException("Proxy route must have at least 1 target")
            }
            dto.targets.map { it.toURI() }
            // TODO if SSL enabled, validate cert
            routeService.putRoute(dto.route, dto.targets)
            exchange.noContent()
        }
    }

    fun removeRoute(exchange: HttpServerExchange) {
        exchange.useBody<RoutePath>(json){ dto ->
            routeService.removeRoute(dto)
            exchange.noContent()
        }
    }
}