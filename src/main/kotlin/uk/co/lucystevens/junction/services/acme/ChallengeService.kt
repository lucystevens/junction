package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Account
import org.shredzone.acme4j.AccountBuilder
import org.shredzone.acme4j.Status
import org.shredzone.acme4j.challenge.Http01Challenge
import org.shredzone.acme4j.util.CSRBuilder
import org.shredzone.acme4j.util.KeyPairUtils
import uk.co.lucystevens.junction.api.handlers.acme.AcmeChallengeHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.AccountService
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.utils.CacheExpiry
import uk.co.lucystevens.junction.utils.PollingHandler
import uk.co.lucystevens.junction.utils.cached
import uk.co.lucystevens.junction.utils.logger
import java.net.URL
import java.time.temporal.ChronoUnit


class ChallengeService(
    private val config: Config,
    private val pollingHandler: PollingHandler,
    private val accountService: AccountService,
    private val challengeHandler: AcmeChallengeHandler,
    sessionProvider: SessionProvider,
    ) {

    private val logger = logger<ChallengeService>()

    private val session = sessionProvider.createSession()

    private val accountLocator by lazy {
        getOrCreateAccount()
    }

    private val account by cached(CacheExpiry(30, ChronoUnit.MINUTES)) {
        login()
    }

    private fun getOrCreateAccount(): String =
        accountService.getAccountLocator() ?: run {
            val email = accountService.getAccountEmail()
            logger.info("Creating account for email $email")
            val accountKeyPair = accountService.getAccountKeyPair()

            val account = AccountBuilder()
                .addContact("mailto:$email")
                .agreeToTermsOfService()
                .useKeyPair(accountKeyPair)
                .create(session)

            account.location.toString().apply {
                logger.info("Account created successfully. Locator: $this")
                accountService.setAccountLocator(this)
            }
        }

    private fun login(): Account{
        logger.info("Logging in to account $accountLocator")
        val keyPair = accountService.getAccountKeyPair()
        val login = session.login(URL(accountLocator), keyPair)
        logger.info("Successfully logged in to account ${login.accountLocation}")
        return login.account
    }

    fun requestCert(domain: String): CertificateResult {
        logger.info("Requesting a new certificate for $domain")
        val order = account.newOrder()
            .domains(domain)
            .create()

        // Process auth challenges
        for (auth in order.authorizations) {
            if (auth.status == Status.PENDING) {
                val challenge = auth.findChallenge(Http01Challenge::class.java)?:
                    throw IllegalStateException("Could not find http-01 challenge.")
                val challengeDomain = auth.identifier.domain

                logger.info("Creating route: ${challenge.token}=${challenge.authorization} for $domain")
                challengeHandler.addChallenge(AcmeChallenge(
                    challengeDomain,
                    challenge.token,
                    challenge.authorization
                ) { logger.debug("Callback triggered") })

                challenge.trigger()

                logger.info("Waiting for authorisation to complete")
                val timeTaken = pollingHandler.waitForComplete(
                    getStatus = { auth.status },
                    update = { auth.update() }
                )
                logger.info("Completed authorisation in ${timeTaken}ms. URl: ${auth.location} Data: ${auth.json}")
            }
        }

        // Wait for order to be ready
        logger.info("Waiting for certificate order to be ready")
        val timeTakenToReady = pollingHandler.waitForReady(
            getStatus = { order.status },
            update = { order.update() }
        )
        logger.info("Order ready in ${timeTakenToReady}ms")

        val keyPair = KeyPairUtils.createKeyPair(config.getRSAKeySize())

        // Create CSR
        logger.info("Creating CSR for $domain")
        val csrb = CSRBuilder().apply {
            addDomain(domain)
            sign(keyPair)
        }
        order.execute(csrb.encoded)

        // Download cert
        logger.info("Waiting for certificate order to complete")
        val timeTakenToComplete = pollingHandler.waitForComplete(
            getStatus = { order.status },
            update = { order.update() }
        )
        if(order.status == Status.INVALID)
            throw IllegalStateException("Order invalid. Response: ${order.error?.asJSON()}")
        logger.info("Completed order in ${timeTakenToComplete}ms")

        val cert = order.certificate?:
            throw IllegalStateException("Could not find certificate for $domain.")

        return CertificateResult(domain, csrb, cert, keyPair)
    }

}