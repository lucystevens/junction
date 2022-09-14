package uk.co.lucystevens.junction.services

import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.handlers.routing.JunctionRouteHandler
import uk.co.lucystevens.junction.db.dao.RouteDao

class RouteService(
    private val junctionRouteHandler: JunctionRouteHandler,
    private val routeDao: RouteDao
) {

    // TODO do we need this cache? The route handler has it's own cache
    private val cache = routeDao.getRoutes().associate {
        it.routePath to it.options
    }.toMutableMap()

    fun getRoutes(): List<Pair<RoutePath, RouteOptions>> = cache.toList()

    fun putRoute(routePath: RoutePath, options: RouteOptions) {
        cache[routePath] = options
        junctionRouteHandler.updateRoute(routePath, options)
        routeDao.putRoute(routePath, options)
    }

    fun removeRoute(routePath: RoutePath) {
        cache.remove(routePath)
        junctionRouteHandler.removeRoute(routePath)
        routeDao.removeRoute(routePath)
    }

}