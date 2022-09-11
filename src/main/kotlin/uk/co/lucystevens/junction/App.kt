package uk.co.lucystevens.junction

import uk.co.lucystevens.junction.cli.AppRunner
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class App(private val args: Array<String>) : KoinComponent {
    private val appRunner by inject<AppRunner>()

    fun run(){
        appRunner.run(args.toList())
    }
}