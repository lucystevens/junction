package uk.co.lucystevens.junction.test

import org.junit.jupiter.api.Test

// depends on pebble-novalidate
class SimpleTest : AcceptanceTest() {

    /**
     * Tests to do:
     *
     *
     * Domains API (happy paths covered by other tests)
     * - list domains
     * - update domain: missing host
     * - update domain: missing redirect
     * - update domain: missing ssl
     * - update domain: switch ssl off
     * - update domain: use ssl, but host doesn't resolve
     * - delete domain
     *
     * Acme Validation (specific test for failed acme validation)
     * - create and get domain with failed acme validation
     * - https proxy to domain with failed validation
     * - http proxy to domain with failed validation doesn't redirect
     */

    @Test
    fun testHttpRoute(){
        app.run()
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
        app.run()
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