package uk.co.lucystevens.api

import io.undertow.Undertow
import uk.co.lucystevens.config.Config
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext


class JunctionServer(
    private val routingHandler: RoutingHandler,
    private val config: Config
    ) {

    fun start(){
        val server = Undertow.builder()
            .addHttpListener(config.getAppPort(), "localhost")
            .build()
        server.start()
    }
}