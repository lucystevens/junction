package uk.co.lucystevens.junction.test

import com.google.gson.JsonObject
import io.javalin.Javalin
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.tls.HandshakeCertificates
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.junit5.AutoCloseKoinTest
import uk.co.lucystevens.junction.App
import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.api.dto.DomainResponseDto
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.dto.RouteTarget
import uk.co.lucystevens.junction.config.Modules
import uk.co.lucystevens.junction.db.models.AppConfig
import uk.co.lucystevens.junction.db.models.Route
import java.net.URI
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// depends on pebble-novalidate
open class AcceptanceTest : KoinTest {

    init {
        System.setProperty("javax.net.ssl.trustStore", "src/acceptanceTest/resources/pebble.jks");
    }

    val dbConn = DriverManager.getConnection(TestModules.dbUrl)

    val defaultHost = "localhost"

    val clientCertificates = HandshakeCertificates.Builder()
        .addPlatformTrustedCertificates()
        .addInsecureHost("localhost")
        .build()

    val client = OkHttpClient.Builder()
        .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
        .build()

    // storing URLs here as there are multiple
    val junctionApi = "http://$defaultHost:8000"
    val junctionHttp = "http://$defaultHost:5002"
    val junctionHttps = "https://$defaultHost:8443"

    companion object {
        private val testServer = Javalin.create().get("/test/*") {
            it.result("Routed to ${it.path()}")
        }

        @JvmStatic @BeforeAll
        fun startTestServer(){
            testServer.start(8001)
        }

        @JvmStatic @AfterAll
        fun stopTestServer(){
            testServer.stop()
        }
    }

    @BeforeEach
    fun startApp(){
        startKoin { modules(Modules.allModules + TestModules.test) }
        App(arrayOf()).run()
    }

    @AfterEach
    fun stopApp(){
        getKoin().get<JunctionServer>().stop()
        stopKoin()
    }

    fun Response.bodyAsString() = body?.string()

    fun Response.assertStatus(expectedStatus: Int) = assertEquals(expectedStatus, code) {
            "Expected status $expectedStatus, but was $code. Body: ${bodyAsString()}"
        }

    fun Response.assertBody(expectedBody: String) =
        assertEquals(expectedBody, bodyAsString())

    fun createEntity(entity: String, requestFile: String){
        val body = readJson("acceptanceTest", "requests/$requestFile.json")
        client.doRequest("$junctionApi/api/$entity"){
            it.post(body.toRequestBody()).addHeader("token", "token")
        }.use {
            it.assertStatus(204)
        }
    }

    inline fun <reified T> getEntities(entity: String): List<T> =
        client.doRequest("$junctionApi/api/$entity"){
            it.get().addHeader("token", "token")
        }.use {
            it.assertStatus(200)
            it.bodyAsString()!!.parse()
        }

    fun createRoute(requestFile: String) = createEntity("routes", requestFile)
    fun createDomain(requestFile: String) = createEntity("domains", requestFile)

    fun getDomains() = getEntities<DomainResponseDto>("domains")
    fun getRoutes() = getEntities<RouteDto>("routes")

    fun waitForCert(host: String){
        var certStatus = "PENDING"
        while (certStatus == "PENDING"){
            certStatus = getDomains().find { it.name == host }!!.ssl.name
            Thread.sleep(500)
        }
        assertEquals("ENABLED", certStatus)
    }

    fun assertDatabaseHasRoute(fromPath: String, toPort: Int, scheme: String = "http"){
        queryOne("SELECT * FROM routes WHERE host='$defaultHost' AND path='$fromPath'"){
            val targets = it.getJson<List<RouteTarget>>("targets")
            assertEquals(1, targets.size, "Expected 1 target for $defaultHost$fromPath but got 2")
            assertEquals(RouteTarget(scheme, defaultHost, toPort), targets[0])
        }
    }

    fun assertConfigEquals(key: String, expectedValue: String){
        queryOne("SELECT * FROM config WHERE key='$key'"){
            assertEquals(expectedValue, it.getString("value"), "For config key $key")
        }
    }

    fun httpRequest(path: String): Response =
        client.doRequest("$junctionHttp$path"){ it.get() }

    fun httpsRequest(path: String): Response =
        client.doRequest("$junctionHttps$path"){ it.get() }

    fun queryRoutes() = query("select * from routes"){
            Route().apply {
                host = it.getString("host")
                path = it.getString("path")
                targets = it.getJson("targets")
            }
        }

    fun queryConfig() = query("select * from config"){
        AppConfig().apply {
            key = it.getString("key")
            value = it.getString("value")
        }
    }

    fun queryOne(sql: String, validate: (ResultSet) -> Unit) =
        dbConn.prepareStatement(sql)
            .executeQuery()
            .let { rs ->
                assertTrue(rs.next())
                validate(rs)
                assertFalse(rs.next())
            }

    fun <T> query(sql: String, parser: (ResultSet) -> T) =
        dbConn.prepareStatement(sql)
            .executeQuery()
            .let { rs ->
                val result = mutableListOf<T>()
                while (rs.next()) {
                    result += parser(rs)
                }
                result
            }

}