package uk.co.lucystevens.junction.api.ssl

import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.services.DomainService
import uk.co.lucystevens.junction.utils.readKeyPair
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class CertificateManager(
    private val config: Config,
    private val domainService: DomainService
) {

    // Can also be default
    private val keyStore = KeyStore.getInstance("jks").apply {
        load(null)
    }

    val sslContext by lazy { createSslContext() }

    fun addCertificate(alias: String, keyPair: KeyPair, certChain: List<X509Certificate>){
        keyStore.setKeyEntry(
            alias,
            keyPair.private,
            config.getCertificatePassword(),
            certChain.toTypedArray())
    }

    private fun loadCertificates() {
        domainService.getDomains()
            .filter { it.ssl && it.certificate != null && it.keyPair != null }
            .forEach {
                addCertificate(
                    it.name,
                    it.keyPair!!.readKeyPair(),
                    it.certificate!!.readCert()
                )
            }
    }

    private fun String.readCert(): List<X509Certificate> {
        val factory = CertificateFactory.getInstance("X509")
        return byteInputStream().use { stream ->
            factory.generateCertificates(stream)
                .map { it as X509Certificate }
        }
    }

    private fun createSslContext(): SSLContext{
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, config.getCertificatePassword())
            loadCertificates()

            return SSLContext.getInstance("TLS").apply {
                init(kmf.keyManagers, null, null)
            }
        }
}