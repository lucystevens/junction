package uk.co.lucystevens.api

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import uk.co.lucystevens.api.error.ErrorHandler
import uk.co.lucystevens.config.Config

class RouteController(
    private val exampleApi: ExampleApi,
    private val adminApi: AdminApi,
    private val infoApi: InfoApi,
    private val errorHandler: ErrorHandler,
    private val config: Config
    ) {

    fun start(){
        val app = Javalin.create().start(config.getAppPort())

        app.routes {
            path("api") {
                path("example"){
                    get(exampleApi::helloWorld)
                }
                path("info"){
                    get(infoApi::getInfo)
                }
                path("admin") {
                    before(adminApi::validateToken)
                }
            }
        }

        app.exception(Exception::class.java) { e, ctx ->
            errorHandler.handle(e, ctx)
        }
    }
}