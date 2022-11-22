package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Certificate
import org.shredzone.acme4j.Order
import org.shredzone.acme4j.Status
import org.shredzone.acme4j.challenge.Http01Challenge
import org.shredzone.acme4j.util.CSRBuilder
import org.shredzone.acme4j.util.KeyPairUtils
import uk.co.lucystevens.junction.api.handlers.acme.AcmeChallengeHandler
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.AccountService
import uk.co.lucystevens.junction.utils.PollingHandler
import uk.co.lucystevens.junction.utils.logger


class ChallengeService(
    private val config: Config,
    private val pollingHandler: PollingHandler,
    private val accountService: AccountService,
    private val challengeHandler: AcmeChallengeHandler,
    private val acmeService: AcmeService,
    ) {

    private val logger = logger<ChallengeService>()

    // login account when first accessed
    // TODO definitely some wierd circular logic here
    private val acme by lazy {
        val accountLocator = getOrCreateAccount()

        logger.info("Logging in to account $accountLocator")
        val keyPair = accountService.getAccountKeyPair()
        acmeService.login(accountLocator, keyPair)
        logger.info("Successfully logged in to account $accountLocator")
        acmeService
    }

    private fun getOrCreateAccount(): String =
        accountService.getAccountLocator() ?: run {
            val email = accountService.getAccountEmail()
            logger.info("Creating account for email $email")
            val accountKeyPair = accountService.getAccountKeyPair()

            val account = acmeService.createAccount(email, accountKeyPair)

            account.location.toString().apply {
                logger.info("Account created successfully. Locator: $this")
                accountService.setAccountLocator(this)
            }
        }


    private fun Order.processChallenges() {
        // Process auth challenges
        for (auth in authorizations) {
            if (auth.status == Status.PENDING) {
                val challenge = auth.findChallenge(Http01Challenge::class.java)?:
                throw IllegalStateException("Could not find http-01 challenge.")
                val challengeDomain = auth.identifier.domain

                logger.info("Creating route: ${challenge.token}=${challenge.authorization} for $challengeDomain")
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
            getStatus = { status },
            update = { update() }
        )
        logger.info("Order ready in ${timeTakenToReady}ms")
    }

    private fun Order.downloadCertificate(): Certificate {
        // Download cert
        logger.info("Waiting for certificate order to complete")
        val timeTakenToComplete = pollingHandler.waitForComplete(
            getStatus = { status },
            update = { update() }
        )
        if(status == Status.INVALID)
            throw IllegalStateException("Order invalid. Response: ${error?.asJSON()}")
        logger.info("Completed order in ${timeTakenToComplete}ms")

        return certificate?:
            throw IllegalStateException("Could not find certificate.")
    }

    fun renewCert(domain: String, csr: CSRBuilder): Certificate {
        logger.info("Renewing certificate for $domain")
        val order = acme.createOrder(domain)

        order.processChallenges()
        order.execute(csr.encoded)

        return order.downloadCertificate()
    }

    fun requestCert(domain: String): CertificateResult {
        logger.info("Requesting a new certificate for $domain")
        val order = acme.createOrder(domain)

        order.processChallenges()

        val keyPair = KeyPairUtils.createKeyPair(config.getRSAKeySize())

        // Create CSR
        logger.info("Creating CSR for $domain")
        val csrb = CSRBuilder().apply {
            addDomain(domain)
            sign(keyPair)
        }
        order.execute(csrb.encoded)

        val cert = order.downloadCertificate()
        return CertificateResult(domain, csrb, cert, keyPair)
    }

}