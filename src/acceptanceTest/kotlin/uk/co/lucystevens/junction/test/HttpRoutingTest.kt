package uk.co.lucystevens.junction.test

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HttpRoutingTest : AcceptanceTest(
    testServers = mapOf("server1" to 8001, "server2" to 8002),
    pebble = Pebble.NONE
) {

    @Test
    fun testRoutingWorksWhenRouteLoadedAtStartUp(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        app.run()

        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    @Test
    fun testRoutingWorksWhenRouteAddedAtRuntime(){
        app.run()
        // add route
        createRoute("createRoute")

        // assert routing
        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }
    }

    @Test
    fun testRoutingWorksWhenMultipleRoutesWithSameHost(){
        // create initial route in database first
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        app.run()

        // add second route
        createRoute("createSecondRoute")

        // assert routing for server 1
        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server1] Routed to /test/subroute")
        }

        // assert routing for server 2
        httpRequest("/anotherRoute").use {
            it.assertStatus(200)
            it.assertBody("[server2] Routed to /anotherRoute")
        }
    }

    @Test
    fun testRoutingFailsWhenRouteDeleted(){
        // create initial route in database first
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        app.run()

        // add second route
        deleteRoute(host = "localhost", path = "/test")

        // assert routing
        httpRequest("/test/subroute").use {
            it.assertStatus(404)
        }
    }

    @Test
    fun testRoutingWorksWhenRouteUpdated(){
        // create initial route in database first
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        app.run()

        // add second route
        updateRoute("updateRoute")

        // assert new routing works
        httpRequest("/test/subroute").use {
            it.assertStatus(200)
            it.assertBody("[server2] Routed to /test/subroute")
        }
    }

    @Test
    fun testRoutingWhenNoRouteExists(){
        // create initial route in database first
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")
        app.run()

        // assert new routing works
        httpRequest("/invalidRoute").use {
            it.assertStatus(404)
        }
    }

    @Test
    fun testRoutingWithMultipleTargets(){
        app.run()

        // add route
        createRoute("createRouteWithMultipleTargets")

        // do 5 requests, and check that they were split between servers
        val responses = (1..5).map {
            httpRequest("/test/subroute").use {
                it.assertStatus(200)
                it.bodyAsString()
            }
        }.sortedBy { it }.toSet()

        val expected = setOf(
            "[server1] Routed to /test/subroute",
            "[server2] Routed to /test/subroute"
        )
        assertEquals(expected, responses)
    }
}