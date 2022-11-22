package uk.co.lucystevens.junction.api

import io.undertow.Undertow
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.utils.logger


class JunctionServer(
    defaultHandlers: DefaultHandlers,
    certificateManager: CertificateManager,
    config: Config
    ) {

    private val logger = logger<JunctionServer>()

    private val server = Undertow.builder()
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

    fun start(){
        logger.info("Starting undertow server")
        server.start()
        logger.info("Undertow server started")
    }

    fun stop(){
        logger.info("Stopping undertow server")
        server.stop()
        logger.info("Undertow server stopped")
    }
}
