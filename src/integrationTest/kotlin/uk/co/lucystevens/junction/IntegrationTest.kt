package uk.co.lucystevens.junction

import io.javalin.Javalin
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.co.lucystevens.junction.api.dto.DomainRequestDto
import uk.co.lucystevens.junction.api.dto.RouteDto
import java.awt.PageAttributes.MediaType


// TODO write a simple integration test for the happy path against a single container
// Use acceptance tests with mocked SSL/ACME for other paths
class IntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun checkRunningInContainer() {
            assertEquals(
                "true",
                System.getenv("INTEGRATION_CONTAINER_CHECK"),
                "Integration tests must be run in the provided Docker container"
            )
        }
    }

    @Test
    fun integrationTest(){
        // run junction, pebble in docker compose (from gradle)
        // run these tests using docker compose too
        // run mock server from integration test

        Javalin.create()
            .get("/test") {
                it.result("test success!")
            }
            .get("/test2") { }
            .start(8000)

        // add ssl domain for integrationTest to junction
        doRequest("http://junction:8000/api/domains"){
            it.post(readJson("requests/createDomain.json").toRequestBody())
                .addHeader("token", "token")
        }.use {
            assertEquals(204, it.code){
                "Expected no content response code, but was ${it.code}. " +
                "Body: ${it.body?.string()}"
            }
        }

        // add proxied route for junction to mock server
        doRequest("http://junction:8000/api/routes"){
            it.post(readJson("requests/createRoute.json").toRequestBody())
                .addHeader("token", "token")
        }.use {
            assertEquals(204, it.code){
                "Expected no content response code, but was ${it.code}. " +
                        "Body: ${it.body?.string()}"
            }
        }
        
        var certStatus = "PENDING"
        while (certStatus == "PENDING"){
            doRequest("http://junction:8000/api/domains"){
                it.get().addHeader("token", "token")
            }.use {
                assertEquals(200, it.code)
                val json = it.body?.string()?.toJson()
                assertNotNull(json)
                certStatus = json!!.asJsonArray
                    .get(0).asJsonObject
                    .get("ssl").asString
            }
            Thread.sleep(500)
        }
        assertEquals("ENABLED", certStatus)

        // test routes and redirects and verify server receipt/response
        doRequest("https://junction:8443/test"){
            it.get()
        }.use {
            assertEquals(200, it.code){
                "Expected no content response code, but was ${it.code}. " +
                        "Body: ${it.body?.string()}"
            }
            assertEquals("test success!", it.body?.string())
        }
    }
}