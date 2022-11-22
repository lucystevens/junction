package uk.co.lucystevens.junction.services.acme

import org.shredzone.acme4j.Account
import org.shredzone.acme4j.Login
import org.shredzone.acme4j.Order
import org.shredzone.acme4j.Session
import java.security.KeyPair

// Wrapper around Acme library methods to make mocking easier for tests
interface AcmeService {

    val session: Session

    fun createAccount(email: String, keyPair: KeyPair): Account
    fun login(accountLocator: String, keyPair: KeyPair)
    fun createOrder(domain: String): Order
}