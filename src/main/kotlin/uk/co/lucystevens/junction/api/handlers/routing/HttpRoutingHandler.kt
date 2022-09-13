package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import uk.co.lucystevens.junction.api.dto.RoutePath
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

    fun updateRoute(routePath: RoutePath, options: RouteOptions){
        if(options.ssl) {
            removeRoute(routePath)
            sslEnforcedHosts.add(routePath.host)
        }
        else {
            sslEnforcedHosts.remove(routePath.host)
            val pathHandler = hostHandler.getOrCreateHandler(routePath.host)
            val proxyHandler = pathHandler.getOrCreateHandler(routePath.path)
            proxyHandler.updateHosts(options.targets.map { it.toURI() })
        }
    }

    fun removeRoute(routePath: RoutePath){
        sslEnforcedHosts.remove(routePath.host)
        hostHandler.getHost(routePath.host)
            ?.removePath(routePath.path)
    }



}