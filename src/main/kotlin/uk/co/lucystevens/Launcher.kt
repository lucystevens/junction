package uk.co.lucystevens

import uk.co.lucystevens.config.Modules
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin { modules(Modules.allModules) }
    App(args).run()
}
