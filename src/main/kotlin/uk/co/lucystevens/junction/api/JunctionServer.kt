package uk.co.lucystevens.junction.api

import io.undertow.Undertow
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.utils.logger


class JunctionServer(
    private val defaultHandlers: DefaultHandlers,
    private val certificateManager: CertificateManager,
    private val config: Config
    ) {

    private val logger = logger<JunctionServer>()

    fun start(){
        logger.info("Building undertow server")
        val server = Undertow.builder()
            .addHttpListener(
                config.getHttpPort(),
                config.getBindAddress(),
                defaultHandlers.httpHandler
            )
            .addHttpListener(
                config.getApiPort(),
                config.getBindAddress(),
                defaultHandlers.apiHandler
            )
            .addHttpsListener(
                config.getHttpsPort(),
                config.getBindAddress(),
                certificateManager.createSslContext(),
                defaultHandlers.httpsHandler
            )
            .build()
        logger.info("Starting undertow server")
        server.start()
        logger.info("Undertow server started")
    }
}}
