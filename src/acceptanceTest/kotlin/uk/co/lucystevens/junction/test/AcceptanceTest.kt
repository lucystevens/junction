package uk.co.lucystevens.junction.test

import io.javalin.Javalin
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.tls.HandshakeCertificates
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.ktorm.database.Database
import uk.co.lucystevens.junction.App
import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.api.dto.DomainResponseDto
import uk.co.lucystevens.junction.api.dto.RouteDto
import uk.co.lucystevens.junction.api.dto.RouteTarget
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.config.Modules
import uk.co.lucystevens.junction.db.models.AppConfig
import uk.co.lucystevens.junction.db.models.Route
import uk.co.lucystevens.junction.services.acme.AcmeService
import java.nio.file.Paths
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.io.path.readText
import kotlin.test.assertTrue

// depends on pebble-novalidate
open class AcceptanceTest(
    val acmeUrl: String = "acme://pebble",
    val secretKey: String = "secret",
    val certPassword: String = "password",
    val adminToken: String = "token",
    val emailAddress: String = "test@mail.com",
    val apiPort: Int = 8000,
    val httpPort: Int = 5002,
    val httpsPort: Int = 8443,
    val bindAddress: String = "0.0.0.0",
    val host: String = "localhost",
    val dbUrl: String = "jdbc:sqlite:file::memory:?cache=shared",
    val testServers: Map<String, Int> = mapOf(),
    val pebble: Pebble = Pebble.STANDARD
) : KoinTest {

    init {
        System.setProperty("javax.net.ssl.trustStore", "src/acceptanceTest/resources/pebble.jks");
    }

    val dbConn = DriverManager.getConnection(dbUrl)

    val clientCertificates = HandshakeCertificates.Builder()
        .addPlatformTrustedCertificates()
        .addTrustedCertificate(Certs.pebble)
        .addTrustedCertificate(Certs.otherRoot)
        .build()

    val client = OkHttpClient.Builder()
        .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
        .build()

    // storing URLs here as there are multiple
    val junctionApi = "http://$host:$apiPort"
    val junctionHttp = "http://$host:$httpPort"
    val junctionHttps = "https://$host:$httpsPort"

    private val javalinServers = testServers.map {
        Javalin.create().get("/*") { ctx ->
            ctx.result("[${it.key}] Routed to ${ctx.path()}")
        }.start(it.value)
    }

    lateinit var app: App

    private val testModule = module {
        single { Config(mapOf(
            "ACME_URL" to acmeUrl,
            "SECRET_KEY" to secretKey,
            "CERT_PASSWORD" to certPassword,
            "ADMIN_TOKEN" to adminToken,
            "EMAIL_ADDRESS" to emailAddress,
            "API_PORT" to apiPort.toString(),
            "HTTP_PORT" to httpPort.toString(),
            "HTTPS_PORT" to httpsPort.toString(),
            "BIND_ADDRESS" to bindAddress
        )) }

        single { Database.connect(
            url = dbUrl
        )}
    }

    @BeforeEach
    fun startApp(){
        startKoin { modules(Modules.allModules + testModule) }
        app = App(arrayOf())
    }

    @AfterEach
    fun stopApp(){
        getKoin().get<JunctionServer>().stop()
        stopKoin()
        javalinServers.forEach { it.stop() }
        dbConn.close()
    }

    fun readFile(dir: String, name: String): String =
        Paths.get("src/acceptanceTest/resources/$dir/$name")
            .readText()

    fun apiRequest(path: String, method: (Request.Builder) -> Unit): Response =
        client.doRequest("$junctionApi$path"){
            method(it.addHeader("token", "token"))
        }

    fun createEntity(entity: String, requestFile: String){
        val body = readJson("acceptanceTest", "requests/$requestFile.json")
        client.doRequest("$junctionApi/api/$entity"){
            it.post(body.toRequestBody()).addHeader("token", "token")
        }.use {
            it.assertStatus(204)
        }
    }

    fun updateEntity(entity: String, requestFile: String){
        val body = readJson("acceptanceTest", "requests/$requestFile.json")
        client.doRequest("$junctionApi/api/$entity"){
            it.put(body.toRequestBody()).addHeader("token", "token")
        }.use {
            it.assertStatus(204)
        }
    }

    fun deleteEntity(entity: String, body: String){
        client.doRequest("$junctionApi/api/$entity"){
            it.delete(body.toJson().toRequestBody()).addHeader("token", "token")
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

    fun updateRoute(requestFile: String) = updateEntity("routes", requestFile)
    fun updateDomain(requestFile: String) = updateEntity("domains", requestFile)

    fun deleteRoute(host: String = this.host, path: String = "/") =
        deleteEntity("routes", """{"host":"$host","path":"$path"}""")
    fun deleteDomain(host: String) =
        deleteEntity("domains", """{"name":"$host"}""")

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

    fun assertDatabaseHasRoute(fromPath: String, toPort: Int, scheme: String = "http") =
        assertDatabaseHasRoutes(fromPath, listOf(toPort), scheme)

    fun assertDatabaseHasRoutes(fromPath: String, toPorts: List<Int>, scheme: String = "http"){
        queryOne("SELECT * FROM routes WHERE host='$host' AND path='$fromPath'"){
            val targets = it.getJson<List<RouteTarget>>("targets")
            assertEquals(toPorts.size, targets.size, "Invalid targets for $host$fromPath.")
            toPorts.forEachIndexed { index, port ->
                assertEquals(RouteTarget(scheme, host, port), targets[index])
            }
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
                assertTrue(rs.next(), "Query '$sql' returned no results")
                validate(rs)
                assertFalse(rs.next(), "Query '$sql' returned more than one result")
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

    fun executeSqlFile(file: String) =
        dbConn.createStatement().executeUpdate(
            Paths.get("src/acceptanceTest/resources/sql/$file.sql")
                .readText()
        )

}