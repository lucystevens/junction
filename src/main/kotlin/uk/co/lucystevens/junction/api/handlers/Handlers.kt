package uk.co.lucystevens.junction.api.handlers

import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.PredicateHandler
import uk.co.lucystevens.junction.api.handlers.acme.AcmeChallengeHandler
import uk.co.lucystevens.junction.api.handlers.api.ApiHandler
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.utils.asHandler
import uk.co.lucystevens.junction.utils.dispatch

fun AcmeRoutingHandler(
    acmeChallengeHandler: AcmeChallengeHandler,
    defaultHandler: HttpHandler
) = PathHandler(defaultHandler)
        .addPrefixPath(
            "/.well-known/acme-challenge/",
            acmeChallengeHandler)

fun HttpsEnabledHandler(
    domainService: DomainService,
    nextHandler: HttpHandler
) = dispatch {
    val httpsEnabled = domainService.getDomain(it.hostName)
        ?.ssl ?: false
    if(httpsEnabled) nextHandler
    else NotFoundHandler()
}

fun HttpsRedirectHandler(
    domainService: DomainService,
    nextHandler: HttpHandler
) = dispatch {
    val url = "${it.requestURL}?${it.queryString}"
    println(url)
    val shouldRedirect = domainService.getDomain(it.hostName)
        ?.redirectToHttps ?: false
    if(shouldRedirect) Handlers.redirect(url)
    else nextHandler
}

fun NotFoundHandler() = asHandler {
    it.statusCode = 404
    it.responseSender.send("Not found")
}

fun ApiRouteHandler(
    apiHandlers: List<ApiHandler>
) = PathHandler(NotFoundHandler()).apply {
    apiHandlers.forEach {
        addPrefixPath(it.path, it)
    }
}
