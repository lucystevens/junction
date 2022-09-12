package uk.co.lucystevens.junction.services

import uk.co.lucystevens.junction.api.dto.Route
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.handlers.routing.HttpRoutingHandler
import uk.co.lucystevens.junction.api.handlers.routing.HttpsRoutingHandler

class RouteService(
    private val httpRoutingHandler: HttpRoutingHandler,
    private val httpsRoutingHandler: HttpsRoutingHandler
) {

    private val cache = mutableMapOf<Route, RouteOptions>()

    fun getRoutes(): List<Pair<Route, RouteOptions>> = cache.toList()

    fun putRoute(route: Route, options: RouteOptions) {
        cache[route] = options
        httpRoutingHandler.updateRoute(route, options)
        httpsRoutingHandler.updateRoute(route, options)
        // TODO persist routes
    }

    fun removeRoute(route: Route) {
        cache.remove(route)
        httpRoutingHandler.removeRoute(route)
        httpsRoutingHandler.removeRoute(route)
        // TODO persist routes
    }

}