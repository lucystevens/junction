package uk.co.lucystevens.config

import org.koin.dsl.module
import uk.co.lucystevens.api.JunctionServer
import uk.co.lucystevens.cli.AppRunner
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
        single { JunctionServer(get()) }
    }

    internal val allModules = listOf(
        utils,
        apis
    )

}