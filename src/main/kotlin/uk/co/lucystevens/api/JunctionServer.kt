package uk.co.lucystevens.api

import io.undertow.Undertow
import uk.co.lucystevens.config.Config


class JunctionServer(
    private val config: Config
    ) {

    fun start(){
        val server = Undertow.builder()
            .addHttpListener(config.getAppPort(), "localhost")
            .build()
        server.start()
    }
}