package uk.co.lucystevens.junction.cli

import uk.co.lucystevens.junction.api.JunctionServer
import uk.co.lucystevens.junction.utils.logger

class AppRunner(private val junction: JunctionServer) {

    private val logger = logger<AppRunner>()

    fun run(args: List<String>){
        logger.info("Starting app")
        junction.start()
    }

}