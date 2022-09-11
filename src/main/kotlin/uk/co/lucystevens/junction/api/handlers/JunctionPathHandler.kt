package uk.co.lucystevens.junction.api.handlers

import io.undertow.server.HttpHandler
import io.undertow.server.handlers.PathHandler

// Path handler that enforces usage by JunctionProxyHandler, and
// stores references to the proxy handlers for each path
class JunctionPathHandler(defaultHandler: HttpHandler? = null) : PathHandler(defaultHandler) {

    private val proxyHandlers = mutableMapOf<String, JunctionProxyHandler>()

    override fun addPrefixPath(path: String, handler: HttpHandler): PathHandler {
        if(handler is JunctionProxyHandler){
            proxyHandlers[path] = handler
            return super.addPrefixPath(path, handler)
        }
        else throw IllegalArgumentException("JunctionPathHandler must be used with JunctionProxyHandler.")
    }

    fun getPath(path: String): JunctionProxyHandler? = proxyHandlers[path]

    fun getOrCreateHandler(path: String): JunctionProxyHandler =
        getPath(path) ?: JunctionProxyHandler().apply {
            addPrefixPath(path, this)
        }

    override fun clearPaths(): PathHandler {
        proxyHandlers.values.forEach { it.removeHosts() }
        proxyHandlers.clear()
        return super.clearPaths()
    }

    override fun removePrefixPath(path: String): PathHandler {
        getPath(path)?.removeHosts()
        proxyHandlers.remove(path)
        return super.removePrefixPath(path)
    }
}