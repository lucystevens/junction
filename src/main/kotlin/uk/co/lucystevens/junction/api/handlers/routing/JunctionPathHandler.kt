package uk.co.lucystevens.junction.api.handlers.routing

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.PathMatcher
import uk.co.lucystevens.junction.api.handlers.NotFoundHandler

// Path handler that enforces usage by JunctionProxyHandler, and
// stores references to the proxy handlers for each path
class JunctionPathHandler : HttpHandler {

    private val pathMatcher = PathMatcher<HttpHandler>()
    private val proxyHandlers = mutableMapOf<String, JunctionProxyHandler>()

    override fun handleRequest(exchange: HttpServerExchange) {
        val match = pathMatcher.match(exchange.relativePath)
        exchange.dispatch(
            match.value ?: NotFoundHandler()
        )
    }

    fun addPath(path: String, handler: JunctionProxyHandler) {
        proxyHandlers[path] = handler
        pathMatcher.addPrefixPath(path, handler)
    }

    fun getPath(path: String): JunctionProxyHandler? = proxyHandlers[path]

    fun addPath(path: String): JunctionProxyHandler =
        getPath(path) ?: JunctionProxyHandler().apply {
            addPath(path, this)
        }

    fun clearPaths() {
        proxyHandlers.values.forEach { it.removeHosts() }
        proxyHandlers.clear()
        pathMatcher.clearPaths()
    }

    fun removePath(path: String) {
        getPath(path)?.removeHosts()
        proxyHandlers.remove(path)
        pathMatcher.removePrefixPath(path)
    }
}