package uk.co.lucystevens.junction.config

import kotlinx.serialization.json.Json
import org.koin.dsl.module
import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.handlers.HttpsRedirectHandler
import uk.co.lucystevens.junction.api.handlers.NotFoundHandler
import uk.co.lucystevens.junction.api.handlers.api.ApiHandler
import uk.co.lucystevens.junction.api.handlers.routing.HttpRoutingHandler
import uk.co.lucystevens.junction.api.handlers.routing.HttpsRoutingHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.api.ssl.KeyManager
import uk.co.lucystevens.junction.api.ssl.KeyUtils
import uk.co.lucystevens.junction.cli.AppRunner
import uk.co.lucystevens.junction.services.RouteService
import java.time.Clock
import kotlin.random.Random

object Modules {

    private val utils = module {
        single { AppRunner(get()) }
        single { Config() }
        single<Clock> { Clock.systemDefaultZone() }
        single<Random> { Random.Default }
        single { Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        } }
    }

    private val handlers = module {
        single { JunctionServer(get(), get(), get(), get(), get()) }
        single { HttpsRoutingHandler(get()) }
        single { HttpRoutingHandler(get()) }
        single { ApiHandler(get(), get(), get(), get()) }
        single { DefaultHandlers(HttpsRedirectHandler(), NotFoundHandler()) }
    }

    private val ssl = module {
        single { KeyUtils() }
        single { KeyManager(get(), get()) }
        single { CertificateManager(get(), get()) }
    }

    private val services = module {
        single { RouteService(get(), get()) }
    }

    internal val allModules = listOf(
        utils,
        handlers,
        ssl,
        services
    )

}