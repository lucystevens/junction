package uk.co.lucystevens.junction.api.ssl

import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.db.models.DomainData
import uk.co.lucystevens.junction.utils.logger
import uk.co.lucystevens.junction.utils.readKeyPair
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509ExtendedKeyManager

class CertificateManager(
    private val config: Config
) {

    private val logger = logger<CertificateManager>()

    // Can also be default
    private val keyStore = KeyStore.getInstance("JKS").apply {
        load(null, config.getSecretKey())
    }

    private val keyManager by lazy {
        UpdatableKeyManager(createKeyManager())
    }

    fun addCertificate(alias: String, keyPair: KeyPair, certChain: List<X509Certificate>){
        keyStore.setKeyEntry(
            alias,
            keyPair.private,
            config.getSecretKey(),
            certChain.toTypedArray())
    }

    fun loadCertificates(domains: List<DomainData>) {
        domains.filter { it.ssl && it.certificate != null && it.keyPair != null }
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
                .map {
                    logger.info("Reading certificate...")
                    logger.info("Subject DN: ${it.subjectDN.name}")
                    logger.info("Issuer DN: ${it.issuerDN.name}")
                    logger.info("Serial No. ${it.serialNumber}")
                    logger.info("Valid From: ${it.notBefore}")
                    logger.info("Valid To: ${it.notAfter}")
                    it
                }
        }
    }

    // THIS WORKS!!!!!!
    fun updateKeyManager() {
        keyManager.keyManager = createKeyManager()
    }

    private fun createKeyManager(): X509ExtendedKeyManager =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).let {
            it.init(keyStore, config.getSecretKey())
            it.keyManagers[0] as X509ExtendedKeyManager
        }

    fun createSslContext(): SSLContext =
        SSLContext.getInstance("TLS").apply {
            init(arrayOf(keyManager), null, null)
        }

}