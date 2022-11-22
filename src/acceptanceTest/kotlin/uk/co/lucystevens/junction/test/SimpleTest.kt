package uk.co.lucystevens.junction.test

import org.junit.jupiter.api.Test

// depends on pebble-novalidate
class SimpleTest : AcceptanceTest() {

    @Test
    fun testHttpRoute(){
        // add route
        createRoute("createRoute")

        // assert database updated
        assertDatabaseHasRoute("/test", 8001, "http")
        assertConfigEquals("account.email", "test@mail.com")

        // assert routing
        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("Routed to /test/subroute")
        }
    }

    @Test
    fun testHttpsRoute(){

        // add ssl domain
        createDomain("createDomain")

        // wait for certificate to resolve
        waitForCert("localhost")

        // add route
        createRoute("createRoute")

        // assert database updated
        assertDatabaseHasRoute("/test", 8001, "http")
        assertConfigEquals("account.email", "test@mail.com")

        val configs = queryConfig()
        configs.forEach { println(it) }


        // assert routing
        httpsRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("Routed to /test/subroute")
        }
    }

}