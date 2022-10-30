package uk.co.lucystevens.junction.services

import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.dto.RouteTarget
import uk.co.lucystevens.junction.api.handlers.routing.JunctionRouteHandler
import uk.co.lucystevens.junction.db.dao.RouteDao

class RouteService(
    private val junctionRouteHandler: JunctionRouteHandler,
    private val routeDao: RouteDao
) {

    // TODO do we need this cache? The route handler has it's own cache
    private val cache by lazy {
        routeDao.getRoutes().associate {
            it.route to it.targets
        }.toMutableMap()
    }

    fun loadRoutes() {
        cache.forEach { (routePath, routeTargets) ->
            junctionRouteHandler.updateRoute(routePath, routeTargets)
        }
    }

    fun getRoutes(): List<Pair<RoutePath, List<RouteTarget>>> = cache.toList()

    fun putRoute(routePath: RoutePath, targets: List<RouteTarget>) {
        cache[routePath] = targets
        junctionRouteHandler.updateRoute(routePath, targets)
        routeDao.putRoute(routePath, targets)
    }

    fun removeRoute(routePath: RoutePath) {
        cache.remove(routePath)
        junctionRouteHandler.removeRoute(routePath)
        routeDao.removeRoute(routePath)
    }

}