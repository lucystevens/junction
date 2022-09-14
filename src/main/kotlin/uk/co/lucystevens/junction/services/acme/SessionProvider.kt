package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Session
import uk.co.lucystevens.junction.config.Config

class SessionProvider(private val config: Config) {
    fun createSession(): Session = Session(config.getAcmeUrl())
}