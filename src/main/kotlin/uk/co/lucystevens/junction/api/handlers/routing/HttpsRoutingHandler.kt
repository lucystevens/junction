package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import uk.co.lucystevens.junction.api.dto.Route
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers

class HttpsRoutingHandler(
    private val defaultHandlers: DefaultHandlers
): HttpHandler {

    private val hostHandler = JunctionHostHandler().apply {
        defaultHandler = defaultHandlers.notFoundHandler
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        hostHandler.handleRequest(exchange)
    }

    fun updateRoute(route: Route, options: RouteOptions){
        if(options.ssl) {
            val pathHandler = hostHandler.getOrCreateHandler(route.host)
            val proxyHandler = pathHandler.getOrCreateHandler(route.path)
            proxyHandler.updateHosts(options.targets.map { it.toURI() })
        } else removeRoute(route)
    }

    fun removeRoute(route: Route){
        hostHandler.getHost(route.host)
            ?.removePath(route.path)
    }



}