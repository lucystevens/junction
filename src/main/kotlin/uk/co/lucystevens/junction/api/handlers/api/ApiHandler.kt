package uk.co.lucystevens.junction.api.handlers.api

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.BadRequestException
import io.undertow.util.Methods
import io.undertow.util.StatusCodes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.lucystevens.junction.api.dto.RoutePath
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.handlers.NotFoundHandler
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.RouteService
import uk.co.lucystevens.junction.utils.asHandler
import uk.co.lucystevens.junction.utils.getHeader
import uk.co.lucystevens.junction.utils.logger
import uk.co.lucystevens.junction.utils.ok

abstract class ApiHandler(
    private val config: Config
    ) : HttpHandler {

    private val tokenHeader = "token"

    val logger = logger<ApiHandler>()
    abstract val path: String
    abstract val routes: Map<ApiRoute, HttpHandler>

    // TODO update to use RoutingHandler for path variables
    override fun handleRequest(exchange: HttpServerExchange) {
        if(exchange.validateToken()){
            exchange.dispatch(
                routes[exchange.route] ?: NotFoundHandler()
            )
        }
        else exchange.statusCode = StatusCodes.UNAUTHORIZED


    }

    // TODO Use exception handler
    fun apiRoute(handler: (exchange: HttpServerExchange) -> Unit): HttpHandler = asHandler {
        try {
            handler(it)
        } catch (e: SerializationException){
            it.statusCode = 400
            logger.error("Request failed.", e)
            it.responseSender.send(e.message?.replace("uk.co.lucystevens.junction.api.dto.", ""))
        } catch(e: BadRequestException){
            it.statusCode = 400
            logger.error("Request failed.", e)
            it.responseSender.send(e.message)

        }
    }


    private fun HttpServerExchange.validateToken(): Boolean {
        val adminToken = config.getAdminToken()
        val token = getHeader(tokenHeader).firstOrNull()
        return adminToken == null || token == adminToken
    }
}