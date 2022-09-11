package uk.co.lucystevens.junction.api

import io.undertow.Handlers
import io.undertow.Undertow
import uk.co.lucystevens.junction.api.handlers.HttpsRedirectHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.config.Config


class JunctionServer(
    private val routingHandler: RoutingHandler,
    private val certificateManager: CertificateManager,
    private val config: Config
    ) {

    fun start(){
        val server = Undertow.builder()
            .addHttpListener(
                config.getHttpsPort(),
                "localhost",
                HttpsRedirectHandler()
            )
            .addHttpsListener(
                config.getHttpsPort(),
                "localhost",
                certificateManager.sslContext,
                routingHandler
            )
            .build()
        server.start()
    }
}