package uk.co.lucystevens.junction.services

import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.handlers.routing.HttpRoutingHandler
import uk.co.lucystevens.junction.api.handlers.routing.HttpsRoutingHandler
import uk.co.lucystevens.junction.db.dao.RouteDao

class RouteService(
    private val httpRoutingHandler: HttpRoutingHandler,
    private val httpsRoutingHandler: HttpsRoutingHandler,
    private val routeDao: RouteDao
) {

    // TODO do we need this cache? The route handler has it's own cache
    private val cache = routeDao.getRoutes().associate {
        it.routePath to it.options
    }.toMutableMap()

    fun getRoutes(): List<Pair<RoutePath, RouteOptions>> = cache.toList()

    fun putRoute(routePath: RoutePath, options: RouteOptions) {
        cache[routePath] = options
        httpRoutingHandler.updateRoute(routePath, options)
        httpsRoutingHandler.updateRoute(routePath, options)
        routeDao.putRoute(routePath, options)
    }

    fun removeRoute(routePath: RoutePath) {
        cache.remove(routePath)
        httpRoutingHandler.removeRoute(routePath)
        httpsRoutingHandler.removeRoute(routePath)
        routeDao.removeRoute(routePath)
    }

}