package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import uk.co.lucystevens.junction.api.dto.Route
import uk.co.lucystevens.junction.api.dto.RouteOptions
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.handlers.acme.AcmeChallengeHandler

class HttpRoutingHandler(
    private val acmeChallengeHandler: AcmeChallengeHandler,
    private val defaultHandlers: DefaultHandlers
): HttpHandler {

    private val sslEnforcedHosts = mutableListOf<String>()
    private val hostHandler = JunctionHostHandler().apply {
        defaultHandler = defaultHandlers.notFoundHandler
    }

    private fun HttpServerExchange.sslEnforced(): Boolean =
        sslEnforcedHosts.contains(hostName)

    private fun HttpServerExchange.isAcmeChallenge(): Boolean =
        requestPath.startsWith("/.well-known/acme-challenge/")

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.dispatch(
            if(exchange.isAcmeChallenge()) acmeChallengeHandler
            else if(exchange.sslEnforced()) defaultHandlers.httpsRedirectHandler
            else hostHandler
        )
    }

    fun updateRoute(route: Route, options: RouteOptions){
        if(options.ssl) {
            removeRoute(route)
            sslEnforcedHosts.add(route.host)
        }
        else {
            sslEnforcedHosts.remove(route.host)
            val pathHandler = hostHandler.getOrCreateHandler(route.host)
            val proxyHandler = pathHandler.getOrCreateHandler(route.path)
            proxyHandler.updateHosts(options.targets.map { it.toURI() })
        }
    }

    fun removeRoute(route: Route){
        sslEnforcedHosts.remove(route.host)
        hostHandler.getHost(route.host)
            ?.removePath(route.path)
    }



}