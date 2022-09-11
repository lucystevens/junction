package uk.co.lucystevens.junction.api.handlers

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient
import io.undertow.server.handlers.proxy.ProxyHandler
import java.net.URI

// Proxy handler wrapping LoadBalancingProxyClient
class JunctionProxyHandler : HttpHandler {

    val proxyClient = LoadBalancingProxyClient()
    val proxyHandler = ProxyHandler.builder()
        .setProxyClient(proxyClient)
        .build()

    override fun handleRequest(exchange: HttpServerExchange?) {
        proxyHandler.handleRequest(exchange)
    }

    // in hosts, but not in targets = add
    // in hosts, and in targets = skip
    // in targets, but not in hosts = remove
    fun updateHosts(hosts: List<URI>) {
        val currentHosts = proxyClient.allTargets.map { URI(it.toString()) }
        // to add
        hosts.filterNot { currentHosts.contains(it) }
            .forEach { proxyClient.addHost(it) }
        // to remove
        currentHosts.filterNot { hosts.contains(it) }
            .forEach { proxyClient.removeHost(it) }
    }

    fun removeHosts() = proxyClient.allTargets
        .map { URI(it.toString()) }
        .forEach { proxyClient.removeHost(it) }
}