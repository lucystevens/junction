package uk.co.lucystevens.junction.test

import org.junit.jupiter.api.Test

class RoutesApiTest : AcceptanceTest(
    pebble = Pebble.NONE
) {

    /**
     * Routes API (happy paths covered by other tests)
     * - list routes
     * - create route
     * - update route
     * - update route: missing host
     * - update route: missing path
     * - update route: empty targets
     * - update route: multiple targets
     * - update route: target with no host
     * - update route: target with no path
     * - delete route
     */

    @Test
    fun testListRoutesReturnsAllRoutes(){
        executeSqlFile("createSchema")
        executeSqlFile("createMultipleRoutes")

        app.run()

        apiRequest("/api/routes"){ it.get() }.use {
            it.assertBody(
                readFile("responses", "listRoutes.json")
            )
            it.assertStatus(200)
        }
    }

    @Test
    fun testCreateRouteWorksWhenPayloadValid(){
        app.run()

        val body = jsonObject {
            obj("route"){
                prop("host", "localhost")
                prop("path", "/test")
            }
            array("targets"){
                obj {
                    prop("host", "localhost")
                    prop("port", 8001)
                }
            }
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.post(body)
        }.use {
            it.assertStatus(204)
        }
        assertDatabaseHasRoute("/test", 8001, "http")
    }

    @Test
    fun testUpdateRouteWorksWhenPayloadValid(){
        executeSqlFile("createSchema")
        executeSqlFile("createRoute")

        app.run()

        val body = jsonObject {
            obj("route"){
                prop("host", "localhost")
                prop("path", "/test")
            }
            array("targets"){
                obj {
                    prop("host", "localhost")
                    prop("port", 8002)
                }
            }
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.post(body)
        }.use {
            it.assertStatus(204)
        }
        assertDatabaseHasRoute("/test", 8002, "http")
    }

    @Test
    fun testCreateRouteFailsWhenMissingHost(){
        app.run()

        // TODO update other tests to use this syntax rather than reading from file
        val body = jsonObject {
            obj("route"){
                prop("path", "/test")
            }
            array("targets"){
                obj {
                    prop("host", "localhost")
                    prop("port", 8001)
                }
            }
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.post(body)
        }.use {
            it.assertStatus(400)
            it.assertBody("Field 'host' is required for type with serial name 'RoutePath', but it was missing at path: \$.route at path: \$.route")
        }
    }

    @Test
    fun testCreateRouteUsesDefaultWhenMissingPath(){
        app.run()

        val body = jsonObject {
            obj("route"){
                prop("host", "localhost")
            }
            array("targets"){
                obj {
                    prop("host", "localhost")
                    prop("port", 8003)
                }
            }
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.post(body)
        }.use {
            it.assertStatus(204)
        }
        assertDatabaseHasRoute("/", 8003)
    }

    @Test
    fun testCreateRouteFailsWhenNoTargetsSpecified(){
        app.run()

        val body = jsonObject {
            obj("route"){
                prop("host", "localhost")
                prop("path", "/test")
            }
            array("targets"){ }
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.post(body)
        }.use {
            it.assertStatus(400)
            it.assertBody("Proxy route must have at least 1 target")
        }
    }

    @Test
    fun testCreateRouteSucceedsWhenMultipleTargetsSpecified(){
        app.run()

        val body = jsonObject {
            obj("route"){
                prop("host", "localhost")
                prop("path", "/test")
            }
            array("targets"){
                obj {
                    prop("host", "localhost")
                    prop("port", 8001)
                }
                obj {
                    prop("host", "localhost")
                    prop("port", 8002)
                }
            }
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.post(body)
        }.use {
            it.assertStatus(204)
        }
        assertDatabaseHasRoutes("/test", listOf(8001, 8002))
    }

    @Test
    fun testDeleteRouteWorksWhenRouteExists(){
        executeSqlFile("createSchema")
        executeSqlFile("createMultipleRoutes")

        app.run()

        val body = jsonObject {
            prop("host", "api.localhost")
            prop("path", "/api")
        }.toRequestBody()

        apiRequest("/api/routes"){
            it.delete(body)
        }.use {
            it.assertStatus(204)
        }
    }
}