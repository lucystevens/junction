package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.handlers.NotFoundHandler

// Entry handler for routing
class JunctionRouteHandler: HttpHandler {

    private val hostHandler = JunctionHostHandler().apply {
        defaultHandler = NotFoundHandler()
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        hostHandler.handleRequest(exchange)
    }

    fun updateRoute(routePath: RoutePath, options: RouteOptions){
        val pathHandler = hostHandler.addHost(routePath.host)
        val proxyHandler = pathHandler.addPath(routePath.path)
        proxyHandler.updateHosts(options.targets.map { it.toURI() })
    }

    fun removeRoute(routePath: RoutePath){
        hostHandler.getHost(routePath.host)
            ?.removePath(routePath.path)
    }

}