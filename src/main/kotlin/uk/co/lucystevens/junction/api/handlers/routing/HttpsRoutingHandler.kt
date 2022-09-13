package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers

// TODO combine with HTTP routing handler
// Use pre-handler to handle SSL specific redirects
class HttpsRoutingHandler(
    private val defaultHandlers: DefaultHandlers
): HttpHandler {

    private val hostHandler = JunctionHostHandler().apply {
        defaultHandler = defaultHandlers.notFoundHandler
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        hostHandler.handleRequest(exchange)
    }

    fun updateRoute(routePath: RoutePath, options: RouteOptions){
        if(options.ssl) {
            val pathHandler = hostHandler.getOrCreateHandler(routePath.host)
            val proxyHandler = pathHandler.getOrCreateHandler(routePath.path)
            proxyHandler.updateHosts(options.targets.map { it.toURI() })
        } else removeRoute(routePath)
    }

    fun removeRoute(routePath: RoutePath){
        hostHandler.getHost(routePath.host)
            ?.removePath(routePath.path)
    }



}