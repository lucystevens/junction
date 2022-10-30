package uk.co.lucystevens.junction.cli

import org.ktorm.database.Database
import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.db.models.createSchema
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.services.RouteService
import uk.co.lucystevens.junction.utils.logger

class AppRunner(
    private val database: Database,
    private val routeService: RouteService,
    private val domainService: DomainService,
    private val certificateManager: CertificateManager,
    private val junction: JunctionServer
    ) {

    private val logger = logger<AppRunner>()

    fun run(args: List<String>){
        logger.info("Setting up database")
        database.createSchema()

        logger.info("Loading routes")
        routeService.loadRoutes()

        logger.info("Loading domains and certificates")
        val domains = domainService.getDomains()
        certificateManager.loadCertificates(domains)

        logger.info("Starting app")
        junction.start()
    }

}