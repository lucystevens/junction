package uk.co.lucystevens.api

import io.javalin.http.Context
import uk.co.lucystevens.api.dto.ServiceInfoResponse
import uk.co.lucystevens.config.Config

class InfoApi(private val config: Config) {

    fun getInfo(ctx: Context) {
        ctx.json(ServiceInfoResponse(
            name = config.getServiceName(),
            version = config.getServiceVersion()
        ))
    }

}