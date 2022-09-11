package uk.co.lucystevens.api

import io.javalin.http.Context

class ExampleApi {

    fun helloWorld(ctx: Context) {
        ctx.json("Hello world")
    }


}