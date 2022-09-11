package uk.co.lucystevens.junction.api

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import uk.co.lucystevens.junction.api.dto.Route
import uk.co.lucystevens.junction.api.handlers.JunctionHostHandler
import java.net.URI

class RoutingHandler(private val notFoundHandler: HttpHandler): HttpHandler {

    private val hostHandler = JunctionHostHandler().apply {
        defaultHandler = notFoundHandler
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        hostHandler.handleRequest(exchange)
    }

    fun updateRoute(route: Route, servers: List<URI>){
        val pathHandler = hostHandler.getOrCreateHandler(route.host)
        val proxyHandler = pathHandler.getOrCreateHandler(route.path)
        proxyHandler.updateHosts(servers)
    }

    fun removeRoute(route: Route){
        hostHandler.getHost(route.host)
            ?.removePrefixPath(route.path)
    }



}