package uk.co.lucystevens.junction.api.handlers.api

import io.undertow.server.HttpServerExchange
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.co.lucystevens.junction.api.dto.DomainRequestDto
import uk.co.lucystevens.junction.api.dto.DomainResponseDto
import uk.co.lucystevens.junction.api.dto.SSLState
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.db.models.DomainData
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.utils.asHandler
import uk.co.lucystevens.junction.utils.noContent
import uk.co.lucystevens.junction.utils.ok
import uk.co.lucystevens.junction.utils.useBody

class DomainsApiHandler(
    private val domainService: DomainService,
    private val json: Json,
    config: Config,
    ) : ApiHandler(config) {

    override val path = "/api/domains"

    override val routes = mapOf(
        get(path) to asHandler { listDomains(it) },
        post(path) to asHandler { updateDomain(it) },
        put(path) to asHandler { updateDomain(it) },
        delete(path) to asHandler { removeDomain(it) }
    )

    fun listDomains(exchange: HttpServerExchange) {
        val domains = domainService.getDomains()
            .map { DomainResponseDto(
                it.name,
                it.redirectToHttps,
                it.sslState,
                it.expiry?.toKotlinInstant()
            ) }
        exchange.ok(json.encodeToString(domains))
    }

    private val DomainData.sslState get() = when {
        ssl && certificate != null -> SSLState.ENABLED
        ssl && certificate == null -> SSLState.PENDING
        else -> SSLState.DISABLED
    }

    // TODO also accept list of domains
    fun updateDomain(exchange: HttpServerExchange) {
        exchange.useBody<DomainRequestDto>(json){ dto ->
            domainService.updateDomain(dto.name)
            exchange.noContent()
        }
    }

    fun removeDomain(exchange: HttpServerExchange) {
        exchange.useBody<DomainRequestDto>(json){ dto ->
            domainService.removeDomain(dto.name)
            exchange.noContent()
        }
    }
}