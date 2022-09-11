package uk.co.lucystevens.api

import io.javalin.http.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test

class ExampleApiTest {

    private val exampleApi = ExampleApi()

    @Test
    fun helloWorld_returnsHelloWorldInResponse(){
        // arrange
        val ctx = mockk<Context>(relaxed = true)

        // action
        exampleApi.helloWorld(ctx)

        // assert
        verify { ctx.json("Hello world") }

    }
}