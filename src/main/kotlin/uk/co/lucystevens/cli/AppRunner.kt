package uk.co.lucystevens.cli

import uk.co.lucystevens.api.JunctionServer
import uk.co.lucystevens.utils.logger

class AppRunner(private val junction: JunctionServer) {

    private val logger = logger<AppRunner>()

    fun run(args: List<String>){
        logger.info("Starting app")
        junction.start()
    }

}