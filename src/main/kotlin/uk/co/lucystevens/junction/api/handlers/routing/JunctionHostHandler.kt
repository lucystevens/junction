package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.handlers.NameVirtualHostHandler

// Host handler that enforces usage by JunctionPathHandler
class JunctionHostHandler : NameVirtualHostHandler() {

    private val pathHandlers = mutableMapOf<String, JunctionPathHandler>()

    // TODO need to load hosts on startup
    override fun addHost(host: String, handler: HttpHandler): NameVirtualHostHandler {
        if(handler is JunctionPathHandler){
            pathHandlers[host] = handler
            return super.addHost(host, handler)
        }
        else throw IllegalArgumentException("JunctionHostHandler must be used with JunctionPathHandler.")
    }

    fun addHost(host: String): JunctionPathHandler =
        getHost(host) ?: JunctionPathHandler().apply {
            addHost(host, this)
        }

    fun getHost(host: String): JunctionPathHandler? = pathHandlers[host]

    override fun removeHost(host: String): NameVirtualHostHandler {
        getHost(host)?.clearPaths()
        pathHandlers.remove(host)
        return super.removeHost(host)
    }
}