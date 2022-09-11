package uk.co.lucystevens.api.handlers

import io.undertow.server.HttpHandler
import io.undertow.server.handlers.NameVirtualHostHandler
import io.undertow.server.handlers.PathHandler

// Host handler that enforces usage by JunctionPathHandler
class JunctionHostHandler : NameVirtualHostHandler() {

    private val pathHandlers = mutableMapOf<String, JunctionPathHandler>()

    override fun addHost(host: String, handler: HttpHandler): NameVirtualHostHandler {
        if(handler is JunctionPathHandler){
            pathHandlers[host] = handler
            return super.addHost(host, handler)
        }
        else throw IllegalArgumentException("JunctionHostHandler must be used with JunctionPathHandler.")
    }

    fun getOrCreateHandler(host: String): JunctionPathHandler =
        getHost(host) ?: JunctionPathHandler(defaultHandler).apply {
            addHost(host, this)
        }

    fun getHost(host: String): JunctionPathHandler? = pathHandlers[host]

    override fun removeHost(host: String): NameVirtualHostHandler {
        getHost(host)?.clearPaths()
        pathHandlers.remove(host)
        return super.removeHost(host)
    }
}