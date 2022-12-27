package uk.co.lucystevens.junction.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpsRoutingTest : AcceptanceTest(
    testServers = mapOf("server1" to 8001, "server2" to 8002),
    pebble = Pebble.NO_VALIDATE
) {

    @Test
    fun testRoutingWorksWhenDomainLoadedAtStartUp(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        executeSqlFile("createFullConfig")
        executeSqlFile("createDomain")
        app.run()

        httpsRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    @Test
    fun testRoutingWorksWhenDomainAddedAtRuntime(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        executeSqlFile("createFullConfig")
        app.run()

        // add ssl domain
        createDomain("createDomain")

        // wait for certificate to resolve
        waitForCert("localhost")

        httpsRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    @Test
    fun testDomainRedirect(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        executeSqlFile("createFullConfig")
        executeSqlFile("createDomain")
        app.run()

        httpRequest("/test/subroute").use {
            assertTrue(it.request.isHttps)
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    @Test
    fun testRoutingWorksWhenDomainUpdatedDuringRuntime(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        executeSqlFile("createNonSSLDomain")
        app.run()

        // assert http proxy
        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }

        // update to ssl domain
        updateDomain("createDomain")

        // wait for certificate to resolve
        waitForCert("localhost")

        httpsRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    // TODO delete domain requires extra unnecessary fields
    @Test
    fun testRoutingFailsWhenDomainDeleted(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        executeSqlFile("createFullConfig")
        executeSqlFile("createDomain")
        app.run()

        deleteDomain("localhost")

        // TODO this doesn't fail because the cert is still in the keystore
        val e = assertThrows<Exception> { httpsRequest("/test/subroute") }
        e.printStackTrace()

        // TODO should we retain routes when a domain is deleted?
        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    @Test
    fun testSslDomainWithoutRouteReturns404(){
        app.run()

        createDomain("createDomain")
        // wait for certificate to resolve
        waitForCert("localhost")

        httpsRequest("/test/subroute").use {
            it.assertStatus(404)
        }
    }

    @Test
    fun testHttpRoutingWorksWhenHttpsRedirectOff(){
        app.run()

        createDomain("createDomainWithoutRedirect")
        createRoute("createRoute")
        // wait for certificate to resolve
        waitForCert("localhost")

        httpRequest("/test/subroute").use {
            assertFalse(it.request.isHttps)
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

}