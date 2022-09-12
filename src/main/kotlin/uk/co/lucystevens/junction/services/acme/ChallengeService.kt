package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Account
import org.shredzone.acme4j.AccountBuilder
import org.shredzone.acme4j.Status
import org.shredzone.acme4j.challenge.Dns01Challenge
import org.shredzone.acme4j.challenge.Http01Challenge
import org.shredzone.acme4j.util.CSRBuilder
import uk.co.lucystevens.junction.api.handlers.acme.AcmeChallengeHandler
import uk.co.lucystevens.junction.api.ssl.CertificateManager
import uk.co.lucystevens.junction.api.ssl.KeyManager
import uk.co.lucystevens.junction.api.ssl.KeyUtils
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.utils.PollingHandler
import uk.co.lucystevens.junction.utils.logger
import java.io.File
import java.io.FileWriter
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.io.path.writer


class ChallengeService(
    private val config: Config,
    private val keyManager: KeyManager,
    private val pollingHandler: PollingHandler,
    private val challengeHandler: AcmeChallengeHandler,
    private val certificateManager: CertificateManager,
    sessionProvider: SessionProvider
    ) {

    private val logger = logger<ChallengeService>()

    private val session = sessionProvider.createSession()

    private val accountLocator by lazy {
        getOrCreateAccount()
    }

    // TODO allow multiple accounts for different domains
    private fun getOrCreateAccount(): String {
        val accountFile = config.getDataDirectory().resolve("account.txt")

        return if(!accountFile.exists()) {
            logger.info("Creating account for email ${config.getEmailAddress()}")
            val accountKeyPair = keyManager.keyPair

            val account = AccountBuilder()
                .addContact("mailto:$config.getEmailAddress()")
                .agreeToTermsOfService()
                .useKeyPair(accountKeyPair)
                .create(session)

            account.location.toString().apply {
                logger.info("Account created successfully. Locator: $this")
                accountFile.writeText(this)
            }
        }
        else {
            accountFile.readText()
        }
    }

    fun login(): Account{
        logger.info("Logging in to account $accountLocator")
        val keyPair = keyManager.keyPair
        val login = session.login(URL(accountLocator), keyPair)
        logger.info("Successfully logged in to account ${login.accountLocation}")
        return login.account
    }

    fun requestCert(account: Account, domain: String): Path {
        logger.info("Requesting a new wildcard certificate for $domain")
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

        // TODO Create private key pair for cert rather than reusing

        // Create CSR
        logger.info("Creating CSR for $domain")
        val outFile = config.getDataDirectory()
            .resolve("$domain.csr")
        val csrb = CSRBuilder().apply {
            addDomain(domain)
            //setOrganization("The Example Organization")
            sign(keyManager.keyPair)
            write(outFile.writer())
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
        val certFile = config.getDataDirectory()
            .resolve("$domain.crt")
        certFile.writer().use {
            cert.writeCertificate(it)
        }

        certificateManager.addCertificate(domain, cert.certificateChain)

        return certFile
    }

}