package uk.co.lucystevens.junction.api.handlers.acme

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.StatusCodes
import org.shredzone.acme4j.challenge.Http01Challenge
import uk.co.lucystevens.junction.services.acme.AcmeChallenge
import uk.co.lucystevens.junction.utils.ok

class AcmeChallengeHandler : HttpHandler {

    private val challenges = mutableListOf<AcmeChallenge>()

    override fun handleRequest(exchange: HttpServerExchange) {
        val domain = exchange.hostName
        val token = exchange.requestPath.substringAfterLast("/")
        val challenge = challenges.find {
            it.domain == domain &&
                    it.token == token
        } ?: throw IllegalArgumentException("No challenge found for domain $domain and token $token")

        exchange.ok(challenge.content, "text/plain")
        //challenges.remove(challenge)
    }

    fun addChallenge(challenge: AcmeChallenge) {
        challenges.add(challenge)
    }
}