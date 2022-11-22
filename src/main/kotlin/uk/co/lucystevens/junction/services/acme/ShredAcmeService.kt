package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Account
import org.shredzone.acme4j.AccountBuilder
import org.shredzone.acme4j.Login
import org.shredzone.acme4j.Order
import org.shredzone.acme4j.Session
import uk.co.lucystevens.junction.config.Config
import java.net.URL
import java.security.KeyPair

// Uses org.shredzone.acme4j implementation
class ShredAcmeService(private val config: Config) : AcmeService {

    override val session: Session = Session(config.getAcmeUrl())
    private lateinit var account: Account

    override fun createAccount(email: String, keyPair: KeyPair): Account =
        AccountBuilder()
            .addContact("mailto:$email")
            .agreeToTermsOfService()
            .useKeyPair(keyPair)
            .create(session)

    override fun login(accountLocator: String, keyPair: KeyPair) {
        this.account = session.login(URL(accountLocator), keyPair).account
    }

    override fun createOrder(domain: String): Order = account.newOrder()
        .domains(domain)
        .create()


}