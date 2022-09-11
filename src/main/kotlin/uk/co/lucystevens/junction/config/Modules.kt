package uk.co.lucystevens.junction.config

import org.koin.dsl.module
import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.api.RoutingHandler
import uk.co.lucystevens.junction.api.handlers.NotFoundHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.api.ssl.KeyManager
import uk.co.lucystevens.junction.api.ssl.KeyUtils
import uk.co.lucystevens.junction.cli.AppRunner
import java.time.Clock
import kotlin.random.Random

object Modules {

    private val utils = module {
        single { AppRunner(get()) }
        single { Config() }
        single<Clock> { Clock.systemDefaultZone() }
        single<Random> { Random.Default }
    }

    private val apis = module {
        single { JunctionServer(get(), get(), get()) }
        single { RoutingHandler(NotFoundHandler()) }
    }

    private val ssl = module {
        single { KeyUtils() }
        single { KeyManager(get(), get()) }
        single { CertificateManager(get(), get()) }
    }

    internal val allModules = listOf(
        utils,
        apis,
        ssl
    )

}