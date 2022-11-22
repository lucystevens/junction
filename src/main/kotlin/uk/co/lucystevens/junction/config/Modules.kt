package uk.co.lucystevens.junction.config

import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.ktorm.database.Database
import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.api.handlers.AcmeRoutingHandler
import uk.co.lucystevens.junction.api.handlers.ApiRouteHandler
import uk.co.lucystevens.junction.api.handlers.DefaultHandlers
import uk.co.lucystevens.junction.api.handlers.HttpsEnabledHandler
import uk.co.lucystevens.junction.api.handlers.HttpsRedirectHandler
import uk.co.lucystevens.junction.api.handlers.acme.AcmeChallengeHandler
import uk.co.lucystevens.junction.api.handlers.api.DomainsApiHandler
import uk.co.lucystevens.junction.api.handlers.api.RoutesApiHandler
import uk.co.lucystevens.junction.api.handlers.routing.JunctionRouteHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.cli.AppRunner
import uk.co.lucystevens.junction.db.dao.ConfigDao
import uk.co.lucystevens.junction.db.dao.DomainDao
import uk.co.lucystevens.junction.db.dao.RouteDao
import uk.co.lucystevens.junction.services.AccountService
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.services.RouteService
import uk.co.lucystevens.junction.services.acme.AcmeService
import uk.co.lucystevens.junction.services.acme.ShredAcmeService
import uk.co.lucystevens.junction.services.acme.ChallengeService
import uk.co.lucystevens.junction.utils.PollingHandler
import java.time.Clock
import kotlin.random.Random

object Modules {

    val utils = module {
        single { AppRunner(get(), get(), get(), get(), get(), get()) }
        single { Config() }
        single<Clock> { Clock.systemDefaultZone() }
        single<Random> { Random.Default }
        single { Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        } }
        single { PollingHandler(clock = get()) }
    }

    val handlers = module {
        single { JunctionServer(get(), get(), get()) }

        // Handlers with their own classes
        single { JunctionRouteHandler() }
        single { AcmeChallengeHandler() }
        single { DomainsApiHandler(get(), get(), get())}
        single { RoutesApiHandler(get(), get(), get()) }

        // Build handler chain using various handlers
        single { buildHandlerChain(get(), get(), get(), get(), get()) }
    }

    // TODO this should be testable somewhere
    fun buildHandlerChain(
        junctionRouteHandler: JunctionRouteHandler,
        acmeChallengeHandler: AcmeChallengeHandler,
        domainsApiHandler: DomainsApiHandler,
        routesApiHandler: RoutesApiHandler,
        domainService: DomainService
    ) = DefaultHandlers(
            httpHandler = AcmeRoutingHandler(
                acmeChallengeHandler,
                HttpsRedirectHandler(
                    domainService,
                    junctionRouteHandler
                )
            ),
            httpsHandler = HttpsEnabledHandler(
                domainService,
                junctionRouteHandler
            ),
            apiHandler = ApiRouteHandler(listOf(
                routesApiHandler,
                domainsApiHandler
            ))
        )

    val services = module {
        // database
        single { setupDatabase(get()) }

        // entity services
        single { RouteService(get(), get()) }
        single { DomainService(get(), get(), get()) }
        single { AccountService(get(), get()) }

        // daos
        single { RouteDao(get()) }
        single { DomainDao(get()) }
        single { ConfigDao(get()) }

        // ssl + certs
        single { CertificateManager(get()) }
        single { ChallengeService(get(), get(), get(), get(), get()) }
        single<AcmeService> { ShredAcmeService(get()) }
    }

    private fun setupDatabase(config: Config): Database {
        return Database.connect(
            url = "jdbc:sqlite:${config.getDatastore().resolve("junction.db")}",
            //driver = "org.sqlite.JDBC"
        )
    }

    val allModules = listOf(
        utils,
        handlers,
        services
    )

}