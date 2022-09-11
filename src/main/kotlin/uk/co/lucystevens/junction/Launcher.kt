package uk.co.lucystevens.junction

import uk.co.lucystevens.junction.config.Modules
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin { modules(Modules.allModules) }
    App(args).run()
}
