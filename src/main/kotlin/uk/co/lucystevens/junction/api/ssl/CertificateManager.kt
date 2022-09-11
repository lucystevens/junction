package uk.co.lucystevens.junction.api.ssl

import uk.co.lucystevens.junction.config.Config
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class CertificateManager(
    private val config: Config,
    private val keyManager: KeyManager) {

    // Can also be default
    private val keyStore = KeyStore.getInstance("jks").apply {
        load(null)
    }

    val sslContext by lazy { createSslContext() }

    fun addCertificate(alias: String, certChain: Array<X509Certificate>){
        keyStore.setKeyEntry(
            alias,
            keyManager.keyPair.private,
            config.getCertificatePassword(),
            certChain)
    }

    private fun createSslContext(): SSLContext{
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, config.getCertificatePassword());

            return SSLContext.getInstance("TLS").apply {
                init(kmf.keyManagers, null, null)
            }
        }
}