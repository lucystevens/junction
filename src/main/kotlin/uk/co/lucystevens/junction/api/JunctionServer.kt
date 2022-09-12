package uk.co.lucystevens.junction.api

import io.undertow.Undertow
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.handlers.HttpsRedirectHandler
import uk.co.lucystevens.junction.api.handlers.api.ApiHandler
import uk.co.lucystevens.junction.api.handlers.routing.HttpRoutingHandler
import uk.co.lucystevens.junction.api.handlers.routing.HttpsRoutingHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.utils.logger


class JunctionServer(
    private val httpRoutingHandler: HttpRoutingHandler,
    private val httpsRoutingHandler: HttpsRoutingHandler,
    private val apiHandler: ApiHandler,
    private val certificateManager: CertificateManager,
    private val config: Config
    ) {

    private val logger = logger<JunctionServer>()

    fun start(){
        logger.info("Building undertow server")
        val server = Undertow.builder()
            .addHttpListener(
                config.getHttpPort(),
                config.getServerHost(),
                httpRoutingHandler
            )
            .addHttpListener(
                config.getApiPort(),
                config.getServerHost(),
                apiHandler
            )
            .addHttpsListener(
                config.getHttpsPort(),
                config.getServerHost(),
                certificateManager.sslContext,
                httpsRoutingHandler
            )
            .build()
        logger.info("Starting undertow server")
        server.start()
        logger.info("Undertow server started")
    }
}