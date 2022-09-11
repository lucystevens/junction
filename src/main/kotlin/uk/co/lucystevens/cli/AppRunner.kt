package uk.co.lucystevens.cli

import uk.co.lucystevens.api.RouteController
import uk.co.lucystevens.logger

class AppRunner(private val routeController: RouteController) {

    private val logger = logger<AppRunner>()

    fun run(args: List<String>){
        logger.info("Starting app")
        routeController.start()
    }

}